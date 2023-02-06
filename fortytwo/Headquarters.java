package fortytwo;

import battlecode.common.*;

public class Headquarters extends Robot {

    Spawner spawner;

    final int INITIAL_DIFF = 0;

    int adamantSent = 0, manaSent = 0, elixirSent = 0;

    final int MAX_MINER_DIFF = 1;

    int minDistToEnemyHQ;

    final int MIN_SOLDIERS;
    //final int MIN_ROUND_ISLANDS = 1500;
    final int MIN_ROUND_ANCHORS = 800;

    final int MIN_ROUND_EMERGENCY = 1600;

    //final int INITIAL_MANA_MINERS = 3;

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
        minDistToEnemyHQ = Math.min(Math.abs(rc.getMapWidth() - 2*rc.getLocation().x), Math.abs(rc.getMapHeight() - 2*rc.getLocation().y));
    }

    void play() throws GameActionException {

        explore.mapdata.updateEnemyMana();
        if (rc.getRoundNum() == 2){
            hComm.computeSymmetries();
            hComm.checkMapType();
            otherComm.updateSymmetry();
        }

        explore.mapdata.checkWellArray();
        explore.mapdata.checkManaWells();
        hComm.sendBuffer();
        Robot.bytecodeDebug +=  "CTINIT: " + Clock.getBytecodeNum() + " ";
        while(tryConstruct()){
            Robot.bytecodeDebug +=  "CT: " + Clock.getBytecodeNum() + " ";
        }
        explore.mapdata.checkCarriers();
    }

    boolean tryConstruct() throws GameActionException{
        int build = buildManager.whatToBuild();
        switch(build){
            case BuildManager.ANCHOR:
                return buildAnchor();
            case BuildManager.AMPLIFIER:
                return constructAmplifier();
            case BuildManager.SOLDIER:
                return constructLauncher();
            case BuildManager.CARRIER:
                return constructCarrier();
            case BuildManager.BOOSTER:
                return constructBooster();
            case BuildManager.DESTABILIZER:
                return constructDestabilizer();

        }
        return false;
    }
    boolean constructBooster() throws GameActionException{
        MapLocation launcherTarget = getSoldierTarget();
        int amt = spawnToTarget(RobotType.BOOSTER, launcherTarget);
        if (amt > 0) {
            return true;
        }
        return false;
    }

    boolean constructDestabilizer() throws GameActionException{
        MapLocation launcherTarget = getSoldierTarget();
        int amt = spawnToTarget(RobotType.DESTABILIZER, launcherTarget);
        if (amt > 0) {
            return true;
        }
        return false;
    }

    boolean constructAmplifier() throws GameActionException{
        MapLocation launcherTarget = getSoldierTarget();
        int amt = spawnToTarget(RobotType.AMPLIFIER, launcherTarget);
        if (amt > 0) {
            return true;
        }
        return false;
    }

    boolean buildAnchor() throws GameActionException {
        if (rc.canBuildAnchor(Anchor.STANDARD)){
            rc.buildAnchor(Anchor.STANDARD);
            buildManager.buildAnchor();
            return true;
        }
        return false;
    }

    boolean constructCarrier() throws GameActionException {
        MapData.WellData carrierTarget = getCarrierTarget();
        if (carrierTarget != null){
            int amt = spawnToTarget(RobotType.CARRIER, carrierTarget.loc);
            if(amt > 0){
                switch(carrierTarget.type){
                    case ADAMANTIUM: adamantSent += amt; break;
                    case MANA: manaSent += amt; break;
                    default: elixirSent += amt; break;
                }
                //carrierTarget.addCarrier();
                rc.setIndicatorDot(carrierTarget.loc, 0, 0, 0);
                hComm.broadcastTarget(carrierTarget.type == ResourceType.ADAMANTIUM ? 0 : 1, carrierTarget.loc);
                return true;
            }
        } else {
            int amt = spawnToTarget(RobotType.CARRIER, rc.getLocation());
            if (amt > 0){
                hComm.broadcastTarget(getMinerDiff() >= 0? 0 : 1, rc.getLocation());
                if (getMinerDiff() >= 0) adamantSent += amt;
                else manaSent +=amt;
                return true;
            }
        }
        return false;
    }

    boolean constructLauncher() throws GameActionException {
        MapLocation launcherTarget = getSoldierTarget();
        int amt = spawnToTarget(RobotType.LAUNCHER, launcherTarget);
        if (amt > 0) return true;
        return false;

    }

    int getMinerDiff(){
        //switch(buildManager.carriersBuilt){
        return manaSent - 3*adamantSent;
        //}
    }

    boolean shouldSendToAdamantium(){
        switch(MAP_TYPE){
            case SMALL:
                if (manaSent < 4) return false;
                return ((manaSent - 4) > adamantSent*4);
            case MEDIUM:
                if (adamantSent == 0) return true;
                if (manaSent < 3) return false;
                return ((manaSent - 3) > (adamantSent-1)*2);
            default:
                if (adamantSent == 0) return true;
                if (manaSent < 3) return false;
                return ((manaSent - 3) > (adamantSent-1));
        }
    }

    boolean valid(MapLocation loc){
        if (loc == null) return false;
        if (rc.getRoundNum() > 5) return true;
        if (Math.abs(loc.x - rc.getLocation().x) >= 12) return false;
        if (Math.abs(loc.y - rc.getLocation().y) >= 12) return false;
        return true;
    }

    MapData.WellData getCarrierTarget() throws GameActionException {
        if (shouldSendToAdamantium()){
            MapData.WellData ans = explore.mapdata.getBestAdamantium(8);
            if (ans != null && valid(ans.loc)){
                return ans;
            }
            /*if (getMinerDiff() <= MAX_MINER_DIFF){
                ans = explore.mapdata.getBestMana(3);
                if (ans != null && valid(ans.loc)) return ans;
            }*/
            return null;
        }
        else{
            MapData.WellData ans = explore.mapdata.getBestMana(8);
            if (ans != null && valid(ans.loc)){
                return ans;
            }
            /*if (getMinerDiff() > -MAX_MINER_DIFF - INITIAL_DIFF) {
                ans = explore.mapdata.getBestAdamantium(3);
                if (ans != null && valid(ans.loc)) return ans;
            }*/
        }
        return null;
    }

    MapLocation getSoldierTarget(){
        return soldierLoc;
    }

    int spawnToTarget(RobotType t, MapLocation target) throws GameActionException {
        if (target == null) target = rc.getLocation();
        int mamt = 4;
        if (t == RobotType.CARRIER) mamt = 2;
        else if (t == RobotType.AMPLIFIER) mamt = 1;
        int amt = spawner.constructRobotGreedy(t, target, mamt);
        if (amt > 0){
            if (t == RobotType.LAUNCHER) buildManager.buildLauncher(amt);
            else if (t == RobotType.CARRIER) buildManager.buildCarrier(amt);
            else if (t == RobotType.AMPLIFIER) buildManager.buildAmplifier(amt);
            else if (t == RobotType.BOOSTER) buildManager.buildBooster(amt);
            else if (t == RobotType.DESTABILIZER) buildManager.buildDestabilizer(amt);
        }
        return amt;
    }

    class BuildManager{

        int carriersBuilt, launchersBuilt, amplifiersBuilt, anchorsBuilt;
        int boostersBuilt = 0, destabilizersBuilt = 0;

        int anchorTurn = -1000;

        static final int TURNS_ANCHOR_EMERGENCY = 50;

        static final int TURNS_ANCHOR = 120;

        static final int ANCHOR = 0, AMPLIFIER = 1, SOLDIER = 2, CARRIER = 3, BOOSTER = 4, DESTABILIZER = 5;

        int whatToBuild() throws GameActionException {
            //if (Constants.MASS_SELFDESTRUCT && rc.getRoundNum() >= 1200) return CARRIER;

            if (shouldBuildDestabilizer() && rc.getResourceAmount(ResourceType.ELIXIR) >= RobotType.DESTABILIZER.getBuildCost(ResourceType.ELIXIR)) return DESTABILIZER;
            if (!shouldBuildDestabilizer() && rc.getResourceAmount(ResourceType.ELIXIR) >= RobotType.BOOSTER.getBuildCost(ResourceType.ELIXIR)) return BOOSTER;

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
            if (rc.getRoundNum() >= MIN_ROUND_EMERGENCY) return true;
            if (otherComm.getSoldiers() < MIN_SOLDIERS) return false;

            int minTurnsSinceLastAnchor = rc.getRoundNum() >= MIN_ROUND_ANCHORS ? TURNS_ANCHOR_EMERGENCY : TURNS_ANCHOR;

            if (rc.getRoundNum() <= anchorTurn + minTurnsSinceLastAnchor) return false;
            return true;
        }

        boolean shouldBuildDestabilizer(){
            return true;
            //return (boostersBuilt+1)*5 > destabilizersBuilt;
        }

        boolean shouldBuildCarrier(){
            if (carriersBuilt < launchersBuilt){
                return true;
            }
            return false;
        }

        boolean shouldBuildAmplifier() throws GameActionException {
            if (carriersBuilt < 3) return false;
            if (rc.getRobotCount() < 20) return false;
            int a = otherComm.getAmplifiers(), l = otherComm.getSoldiers();
            if (16 + 24*a <= l) return true;
            //if (12 + 24*amplifiersBuilt <= launchersBuilt) return true;
            return false;
        }

        void buildAmplifier(int amt) throws GameActionException {
            amplifiersBuilt += amt;
            otherComm.reportAmplifier();
        }

        void buildAnchor(){
            anchorsBuilt++;
            anchorTurn = rc.getRoundNum();
        }

        void buildBooster(int amt){
            boostersBuilt += amt;
        }
        void buildDestabilizer(int amt){
            destabilizersBuilt += amt;
        }

        void buildCarrier(int amt){
            carriersBuilt += amt;
        }

        void buildLauncher(int amt) throws GameActionException {
            launchersBuilt += amt;
            otherComm.reportSoldier(amt);
        }

    }

}
