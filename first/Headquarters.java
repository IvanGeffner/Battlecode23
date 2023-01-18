package first;

import battlecode.common.*;

public class Headquarters extends Robot {

    Spawner spawner;

    int adamantSent = 0, manaSent = 0, elixirSent = 0;

    int adamantSavings = 0, manaSavings = 0;

    final int MIN_SOLDIERS;
    //final int MIN_ROUND_ISLANDS = 1500;
    final int MIN_ROUND_ISLANDS = 1200;




    Headquarters(RobotController rc) throws GameActionException {
        super(rc);
        spawner = new Spawner();
        if (hComm.hqIndex == 1){
            explore.mapdata.firstToWrite = true;
        }
        explore.mapdata.addWells();
        MIN_SOLDIERS = 30 + rc.getMapWidth()*rc.getMapHeight()/15;
        //MIN_SOLDIERS = 50;
    }

    void play() throws GameActionException {
        hComm.sendBuffer();
        tryConstruct();
        explore.mapdata.checkCarriers();
    }

    void tryConstruct() throws GameActionException{
        if (shouldConstructAnchor()) buildAnchor();
        if (shouldPriorizeCarrier()) {
            constructCarrier();
            constructLauncher();
        }
        else {
            constructLauncher();
            constructCarrier();
        }
    }

    void buildAnchor() throws GameActionException {
        if (rc.canBuildAnchor(Anchor.STANDARD)){
            rc.buildAnchor(Anchor.STANDARD);
            manaSavings = 0;
            adamantSavings = 0;
        }
        else {
            manaSavings = Anchor.STANDARD.manaCost;
            adamantSavings = Anchor.STANDARD.adamantiumCost;
        }
    }

    boolean shouldConstructAnchor() throws GameActionException {
        if (rc.getNumAnchors(Anchor.STANDARD) > 0) return false;
        if (otherComm.getSoldiers() < MIN_SOLDIERS && rc.getRoundNum() < MIN_ROUND_ISLANDS) return false;
        return true;
    }

    void constructCarrier() throws GameActionException {
        if (rc.getResourceAmount(ResourceType.ADAMANTIUM) <= RobotType.CARRIER.getBuildCost(ResourceType.ADAMANTIUM) + adamantSavings) return;
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
            }
        } else {
            int id = spawnToTarget(RobotType.CARRIER, rc.getLocation());
            if (id >= 0) hComm.broadcastTarget(id, rc.getLocation());
        }
    }

    void constructLauncher() throws GameActionException {
        if (rc.getResourceAmount(ResourceType.MANA) <= RobotType.LAUNCHER.getBuildCost(ResourceType.MANA) + manaSavings) return;
        MapLocation launcherTarget = getSoldierTarget();
        int id = spawnToTarget(RobotType.LAUNCHER, launcherTarget);
        if (id >= 0) otherComm.reportSoldier();
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
        return rc.getLocation();
    }

    int spawnToTarget(RobotType t, MapLocation target) throws GameActionException {
        if (target == null) target = rc.getLocation();
        int id = spawner.constructRobotGreedy(t, target);
        if (id >= 0){
            if (t == RobotType.LAUNCHER) ++launchers;
            else if (t == RobotType.CARRIER) ++carriers;
        }
        return id;
    }



    int carriers = 0;
    int launchers = 0;

    boolean shouldPriorizeCarrier(){
        if (carriers >= 100) return false;
        return carriers <= launchers + 5;
    }

}
