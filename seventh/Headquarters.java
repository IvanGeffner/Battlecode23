package seventh;

import battlecode.common.*;

public class Headquarters extends Robot {

    Spawner spawner;

    int adamantSent = 0, manaSent = 0, elixirSent = 0;

    final int MIN_SOLDIERS;
    //final int MIN_ROUND_ISLANDS = 1500;
    final int MIN_ROUND_ANCHORS = 800;

    final int MIN_ROUND_EMERGENCY = 1600;

    MapLocation soldierLoc;

    BuildManager buildManager;




    Headquarters(RobotController rc) throws GameActionException {
        super(rc);
        spawner = new Spawner();
        if (hComm.hqIndex == 1){
            explore.mapdata.firstToWrite = true;
        }
        explore.mapdata.addWells();
        MIN_SOLDIERS = 30 + rc.getMapWidth()*rc.getMapHeight()/15;
        soldierLoc = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        buildManager = new BuildManager();
        //MIN_SOLDIERS = 50;
    }

    void play() throws GameActionException {
        hComm.sendBuffer();
        tryConstruct();
        explore.mapdata.checkCarriers();
    }

    void tryConstruct() throws GameActionException{
        int build = buildManager.whatToBuild();
        switch(build){
            case BuildManager.ANCHOR:
                buildAnchor();
                break;
            case BuildManager.AMPLIFIER:
                constructAmplifier();
                break;
            case BuildManager.SOLDIER:
                constructLauncher();
                break;
            case BuildManager.CARRIER:
                constructCarrier();
                break;

        }
    }

    void constructAmplifier() throws GameActionException{
        MapLocation launcherTarget = getSoldierTarget();
        int id = spawnToTarget(RobotType.AMPLIFIER, launcherTarget);
        //if (id >= 0) otherComm.reportSoldier(); TODO: report amplifier
    }

    void buildAnchor() throws GameActionException {
        if (rc.canBuildAnchor(Anchor.STANDARD)){
            rc.buildAnchor(Anchor.STANDARD);
            buildManager.buildAnchor();
        }
    }

    boolean shouldConstructAnchor() throws GameActionException {
        if (rc.getNumAnchors(Anchor.STANDARD) > 0) return false;
        if (otherComm.getSoldiers() < MIN_SOLDIERS && rc.getRoundNum() < MIN_ROUND_ANCHORS) return false;
        return true;
    }

    void constructCarrier() throws GameActionException {
        MapData.WellData carrierTarget = getCarrierTarget();
        if (carrierTarget != null){
            int id = spawnToTarget(RobotType.CARRIER, carrierTarget.loc);
            if(id >= 0){
                switch(carrierTarget.type){
                    case ADAMANTIUM: ++adamantSent; break;
                    case MANA: ++manaSent; break;
                    default: ++elixirSent; break;
                }
                //carrierTarget.addCarrier();
                hComm.broadcastTarget(id, carrierTarget.loc);
                buildManager.buildCarrier();
            }
        } else {
            int id = spawnToTarget(RobotType.CARRIER, rc.getLocation());
            if (id >= 0){
                hComm.broadcastTarget(id, rc.getLocation());
                buildManager.buildCarrier();
            }
        }
    }

    void constructLauncher() throws GameActionException {
        MapLocation launcherTarget = getSoldierTarget();
        int id = spawnToTarget(RobotType.LAUNCHER, launcherTarget);
        if (id >= 0){
            otherComm.reportSoldier();
            buildManager.buildLauncher();
        }
    }

    MapData.WellData getCarrierTarget(){
        if (adamantSent <= manaSent){
            MapData.WellData ans = explore.mapdata.getBestAdamantium();
            if (ans != null) return ans;
            ans = explore.mapdata.getBestMana();
            if (ans != null) return ans;
        }
        else{
            MapData.WellData ans = explore.mapdata.getBestMana();
            if (ans != null) return ans;
            ans = explore.mapdata.getBestAdamantium();
            if (ans != null) return ans;
        }
        return null;
    }

    MapLocation getSoldierTarget(){
        return soldierLoc;
    }

    int spawnToTarget(RobotType t, MapLocation target) throws GameActionException {
        if (target == null) target = rc.getLocation();
        int id = spawner.constructRobotGreedy(t, target);
        if (id >= 0){
            if (t == RobotType.LAUNCHER) buildManager.buildLauncher();
            else if (t == RobotType.CARRIER) buildManager.buildLauncher();
            else if (t == RobotType.AMPLIFIER) buildManager.buildAmplifier();
        }
        return id;
    }

    class BuildManager{

        int carriersBuilt, launchersBuilt, amplifiersBuilt, anchorsBuilt;

        int anchorTurn = -1000;

        static final int TURNS_ANCHOR = 50;

        static final int ANCHOR = 0, AMPLIFIER = 1, SOLDIER = 2, CARRIER = 3;

        int whatToBuild() throws GameActionException {
            int sAd = 0, sMa = 0;
            if (shouldBuildAnchor()){
                if (rc.getResourceAmount(ResourceType.ADAMANTIUM) >= sAd + Anchor.STANDARD.adamantiumCost){
                    if (rc.getResourceAmount(ResourceType.MANA) >= sMa + Anchor.STANDARD.manaCost) {
                        return ANCHOR;
                    }
                }
                sAd += Anchor.STANDARD.adamantiumCost;
                sMa += Anchor.STANDARD.manaCost;
            }
            if (shouldBuildAmplifier()){
                if (rc.getResourceAmount(ResourceType.ADAMANTIUM) >= sAd + RobotType.AMPLIFIER.getBuildCost(ResourceType.ADAMANTIUM)){
                    if (rc.getResourceAmount(ResourceType.MANA) >= sMa + RobotType.AMPLIFIER.getBuildCost(ResourceType.MANA)) {
                        return AMPLIFIER;
                    }
                }
                sAd += RobotType.AMPLIFIER.getBuildCost(ResourceType.ADAMANTIUM);
                sMa += RobotType.AMPLIFIER.getBuildCost(ResourceType.MANA);
            }
            if (shouldBuildCarrier()){
                if (rc.getResourceAmount(ResourceType.ADAMANTIUM) >= sAd + RobotType.CARRIER.getBuildCost(ResourceType.ADAMANTIUM)){
                    return CARRIER;
                }
            }

            if (rc.getResourceAmount(ResourceType.MANA) >= sMa + RobotType.LAUNCHER.getBuildCost(ResourceType.MANA)) {
                return SOLDIER;
            }

            if (rc.getResourceAmount(ResourceType.ADAMANTIUM) >= sAd + RobotType.CARRIER.getBuildCost(ResourceType.ADAMANTIUM)){
                return CARRIER;
            }
            return -1;
        }

        boolean shouldBuildAnchor() throws GameActionException {
            if (rc.getNumAnchors(Anchor.STANDARD) > 0) return false;
            if (rc.getRoundNum() > MIN_ROUND_EMERGENCY) return true;
            if (otherComm.getSoldiers() < MIN_SOLDIERS || rc.getRoundNum() < MIN_ROUND_ANCHORS) return false;
            if (rc.getRoundNum() <= anchorsBuilt + TURNS_ANCHOR) return false;
            return true;
        }

        boolean shouldBuildCarrier(){
            if (carriersBuilt <= launchersBuilt+5){
                Constants.indicatorString += "Should build carrier";
                return true;
            }
            Constants.indicatorString += "Should NOT build carrier";
            return false;
        }

        boolean shouldBuildAmplifier(){
            return false;
        }

        void buildAmplifier(){
            amplifiersBuilt++;
        }

        void buildAnchor(){
            anchorsBuilt++;
            anchorTurn = rc.getRoundNum();
        }

        void buildCarrier(){
            ++carriersBuilt;
        }

        void buildLauncher(){
            ++launchersBuilt;
        }

    }

}
