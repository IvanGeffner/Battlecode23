package second;

import battlecode.common.*;

public class Launcher extends Robot {

    MicroAttacker microAttacker;
    static Team opponent;

    Launcher(RobotController rc) throws GameActionException {
        super(rc);
        opponent = rc.getTeam().opponent();
        microAttacker = new MicroAttacker();
        generateHQArray();
    }

    void play() throws GameActionException {
        while(attack()){}
        if (!microAttacker.doMicro()) moveToTarget();
        while(attack()){}
    }

    void moveToTarget() throws GameActionException {
        MapLocation target = getClosestEnemy();
        if (target == null) target = getClosestEnemyIsland();
        if (target == null) target = getBestEnemyBase();
        if (target == null) target = explore.getExploreTarget();
        pathfinder.moveTo(target);
    }

    MapLocation getClosestEnemy() throws GameActionException {
        MoveTarget bestMove = null;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.HEADQUARTERS) continue;
            MoveTarget t = new MoveTarget(r);
            if(t.isBetterThan(bestMove)) bestMove = t;
        }
        if (bestMove == null) return null;
        return bestMove.mloc;
    }

    boolean attack() throws GameActionException {
        if (!rc.isActionReady()) return false;
        AttackTarget bestAttacker = null;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.HEADQUARTERS) continue;
            if (!rc.canAttack(r.getLocation())) continue;
            AttackTarget t = new AttackTarget(r);
            if(t.isBetterThan(bestAttacker)) bestAttacker = t;
        }
        if (bestAttacker != null){
            if (rc.canAttack(bestAttacker.mloc)) rc.attack(bestAttacker.mloc);
            return true;
        }
        return false;
    }

    MapLocation getClosestEnemyIsland() throws GameActionException {
        int[] islands = rc.senseNearbyIslands();
        MapLocation ans = null;
        int d = rc.getType().visionRadiusSquared;
        MapLocation myLoc = rc.getLocation();
        for (int id : islands){
            if (rc.senseTeamOccupyingIsland(id) != opponent) continue;
            MapLocation[] iLocs = rc.senseNearbyIslandLocations(d, id);
            for (MapLocation m : iLocs){
                if (ans == null || m.distanceSquaredTo(myLoc) < d){
                    d = m.distanceSquaredTo(myLoc);
                    ans = m;
                }
            }
        }
        return ans;
    }

    class AttackTarget{
        RobotType type;
        int health;
        boolean attacker = false;
        MapLocation mloc;

        boolean isBetterThan(AttackTarget t){
            if (t == null) return true;
            if (attacker & !t.attacker) return true;
            if (!attacker & t.attacker) return false;
            return health <= t.health;
        }

        AttackTarget(RobotInfo r){
            type = r.getType();
            health = r.getHealth();
            mloc = r.getLocation();
            switch(type){
                case LAUNCHER:
                case BOOSTER:
                case DESTABILIZER:
                    attacker = true;
                default:
                    break;
            }
        }
    }

    class MoveTarget{
        RobotType type;
        int health;
        int priority;
        MapLocation mloc;

        boolean isBetterThan(MoveTarget t){
            if (t == null) return true;
            if (priority > t.priority) return true;
            if (priority < t.priority) return true;
            return health <= t.health;
        }

        MoveTarget(RobotInfo r){
            this.type = r.getType();
            this.health = r.getHealth();
            this.mloc = r.getLocation();
            switch (r.getType()){
                case LAUNCHER:
                    priority = 5;
                    break;
                case BOOSTER:
                    priority = 6;
                    break;
                case DESTABILIZER:
                    priority = 4;
                    break;
                case CARRIER:
                    priority = 2;
                    break;
                case AMPLIFIER:
                    priority = 1;
                    break;
            }
        }
    }

    PossibleHQLocation[] hqArray;

    void generateHQArray() throws GameActionException {
        int hqArrayIndex = 0;
        hqArray = new PossibleHQLocation[12];
        for (int i = 0; i < 4; ++i){
            MapLocation loc = hComm.getHQLocation((hComm.hqIndex + i)%4);
            if (loc == null) continue;
            hqArray[hqArrayIndex++] = new PossibleHQLocation(loc, ROT);
            hqArray[hqArrayIndex++] = new PossibleHQLocation(loc, HOR);
            hqArray[hqArrayIndex++] = new PossibleHQLocation(loc, VER);
        }
    }


    MapLocation getBestEnemyBase() throws GameActionException {
        for (int i = 0; i < hqArray.length; ++i){
            if (hqArray[i] == null) return null;
            if (hqArray[i].checkConfirmed()) return hqArray[i].loc;
        }
        return null;
    }

    static final int HOR = 1;
    static final int VER = 2;
    static final int ROT = 3;

    class PossibleHQLocation{

        final MapLocation loc;
        int confirmation = 0;

        int sym;
        PossibleHQLocation (MapLocation myHQLoc, int sym){
            this.sym = sym;
            switch(sym){
                case HOR:
                    loc = new MapLocation(rc.getMapWidth() - myHQLoc.x - 1, myHQLoc.y);
                    break;
                case VER:
                    loc = new MapLocation(myHQLoc.x, rc.getMapHeight() - myHQLoc.y - 1);
                    break;
                default:
                    loc = new MapLocation(rc.getMapWidth() - myHQLoc.x - 1, rc.getMapHeight() - myHQLoc.y - 1);
                    break;
            }
        }

        boolean checkConfirmed() throws GameActionException {
            switch(confirmation){
                case -1: return false;
                case 1: return true;
                default:
                    if (otherComm.sym != null){
                        if (otherComm.sym == sym){
                            confirmation = 1;
                            return true;
                        }
                        else{
                            confirmation = -1;
                            return false;
                        }
                    } else {
                        switch(sym){
                            case HOR: if (!otherComm.horizontal){
                                confirmation = -1;
                                return false;
                            }
                            case VER: if (!otherComm.vertical){
                                confirmation = -1;
                                return false;
                            }
                            case ROT: if (!otherComm.rotational){
                                confirmation = -1;
                                return false;
                            }
                        }
                    }
                    int x = explore.mapdata.isEnemyHQ(loc);
                    confirmation = x;
                    return confirmation >= 0;
            }
        }
    }


}