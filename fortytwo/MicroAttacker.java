package fortytwo;

import battlecode.common.*;

public class MicroAttacker {

    final int INF = 1000000;
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    static int myRange;
    static int myVisionRange;

    static int myDMG = RobotType.LAUNCHER.damage; //TODO

    static final int RANGE_EXTENDED_LAUNCHER = 26;
    static final int RANGE_LAUNCHER = 16;

    //static double myDPS;
    //static double[] DPS = new double[]{0, 0, 0, 0, 0, 0, 0};
    //static int[] rangeExtended = new int[]{0, 0, 0, 0, 0, 0, 0};

    static final Direction[] dirs = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    final static int MAX_MICRO_BYTECODE_REMAINING = 2000;

    static RobotController rc;

    MicroAttacker(){
        this.rc = Robot.rc;
        myRange = rc.getType().actionRadiusSquared;
        myVisionRange = rc.getType().visionRadiusSquared;
    }

    static int currentRangeExtended;
    static double currentActionRadius;
    static boolean canAttack;
    static MapLocation currentLoc;
    static int currentDMG = 0;

    boolean doMicro(){
        try {
            if (!rc.isMovementReady()) return false;
            shouldPlaySafe = false;
            RobotInfo[] units = Robot.enemies;
            if (units.length == 0) return false;
            canAttack = rc.isActionReady();
            if (rc.getType() == RobotType.AMPLIFIER) canAttack = false;

            int uIndex = units.length;
            while (uIndex-- > 0){
                RobotInfo r = units[uIndex];
                switch(r.getType()){
                    case LAUNCHER:
                        shouldPlaySafe = true;
                        break;
                    default:
                        break;
                }
            }

            for (int i = 0; i < Robot.hComm.numBases; ++i){
                currentLoc = Robot.otherComm.getLauncherLoc(i);
                if (currentLoc != null && rc.getLocation().distanceSquaredTo(currentLoc) <= 52) {
                    shouldPlaySafe = true;
                    break;
                }
            }

            if (!shouldPlaySafe) return false;

            alwaysInRange = false;
            if (!canAttack) alwaysInRange = true;

            MicroInfo[] microInfo = new MicroInfo[9];
            for (int i = 0; i < 9; ++i) microInfo[i] = new MicroInfo(dirs[i]);

            for (RobotInfo unit : units) {
                if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
                if (unit.getType() != RobotType.LAUNCHER) continue; //TODO: maybe add carriers
                currentLoc = unit.getLocation();
                switch(unit.getType()){
                    case LAUNCHER:
                        currentDMG = RobotType.LAUNCHER.damage;
                        currentRangeExtended = RANGE_EXTENDED_LAUNCHER;
                        currentActionRadius = RANGE_LAUNCHER;
                        break;
                    default:
                        currentDMG = 0;
                        break;
                }
                microInfo[0].updateEnemy();
                microInfo[1].updateEnemy();
                microInfo[2].updateEnemy();
                microInfo[3].updateEnemy();
                microInfo[4].updateEnemy();
                microInfo[5].updateEnemy();
                microInfo[6].updateEnemy();
                microInfo[7].updateEnemy();
                microInfo[8].updateEnemy();
            }

            currentDMG = RobotType.LAUNCHER.damage;
            currentRangeExtended = RANGE_EXTENDED_LAUNCHER;
            currentActionRadius = RANGE_LAUNCHER;
            for (int i = 0; i < Robot.hComm.numBases; ++i){
                currentLoc = Robot.otherComm.getLauncherLoc(i);
                if (currentLoc == null) continue;
                if (rc.canSenseLocation(currentLoc)) continue;
                microInfo[0].updateEnemy();
                microInfo[1].updateEnemy();
                microInfo[2].updateEnemy();
                microInfo[3].updateEnemy();
                microInfo[4].updateEnemy();
                microInfo[5].updateEnemy();
                microInfo[6].updateEnemy();
                microInfo[7].updateEnemy();
                microInfo[8].updateEnemy();
            }

            units = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
            for (RobotInfo unit : units) {
                if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
                if (unit.getType() != RobotType.LAUNCHER) continue; //TODO: maybe add carriers
                currentLoc = unit.getLocation();
                switch(unit.getType()){
                    case LAUNCHER:
                        currentDMG = RobotType.LAUNCHER.damage;
                        currentActionRadius = RANGE_LAUNCHER;
                        break;
                    default:
                        currentDMG = 0;
                        break;
                }
                microInfo[0].updateAlly();
                microInfo[1].updateAlly();
                microInfo[2].updateAlly();
                microInfo[3].updateAlly();
                microInfo[4].updateAlly();
                microInfo[5].updateAlly();
                microInfo[6].updateAlly();
                microInfo[7].updateAlly();
                microInfo[8].updateAlly();
            }

            MicroInfo bestMicro = microInfo[8];
            for (int i = 0; i < 8; ++i) {
                if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
            }

            boolean b = apply(bestMicro);
            if (rc.senseMapInfo(rc.getLocation()).hasCloud()){
                if (bestMicro != null && bestMicro.target != null){
                    while(rc.canAttack(bestMicro.target)) rc.attack(bestMicro.target);
                }
            }

            return b;

        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    boolean apply(MicroInfo bestMicro) throws GameActionException {
        if (bestMicro.dir == Direction.CENTER) return true;

        if (rc.canMove(bestMicro.dir)) {
            Robot.move(bestMicro.dir);
            return true;
        }
        return false;
    }

    class MicroInfo{
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;

        int canLandHit = 0;

        int launchersAttackRange = 0;

        int launchersVisionRange = 0;

        int possibleEnemyLaunchers = 0;

        int minDistToAlly = INF;

        MapLocation target = null;

        boolean canMove = true;
        boolean cloud = false;

        public MicroInfo(Direction dir) throws GameActionException {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            if (canMove) {
                if (AdjacentTiles.dirDanger[dir.ordinal()].closeToEnemyHQ) canMove = false;
                if (rc.senseMapInfo(location).hasCloud()) cloud = true;
            }
            else{
                minDistanceToEnemy = INF;
                //alliesTargeting += myDMG;
            }
        }

        void updateEnemy(){
            if (!canMove) return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist < minDistanceToEnemy)  minDistanceToEnemy = dist;
            if (cloud){
                if (dist <= GameConstants.CLOUD_VISION_RADIUS_SQUARED){
                    launchersAttackRange++;
                    launchersVisionRange++;
                    possibleEnemyLaunchers++;
                }
            } else {
                if (dist <= currentActionRadius) launchersAttackRange++;
                if (dist <= 20) launchersVisionRange++;
                if (dist <= RANGE_EXTENDED_LAUNCHER) possibleEnemyLaunchers++;
            }
            if (dist <= myRange && canAttack){
                canLandHit = 1;
                target = currentLoc;
                //possibleAllies++;
            }
            //if (dist <= RANGE_EXTENDED_LAUNCHER) enemiesTargeting += currentDMG; //TODO carriers?
        }

        void updateAlly(){
            if (!canMove) return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist < minDistToAlly) minDistToAlly = dist;
            //if (dist <= 2) alliesTargeting += currentDMG;
        }

        boolean inRange(){
            if (alwaysInRange) return true;
            return minDistanceToEnemy <= myRange;
        }

        //equal => true
        boolean isBetter(MicroInfo M){

            //if (safe() > M.safe()) return true;
            //if (safe() < M.safe()) return false;

            if (canMove && !M.canMove) return true;
            if (!canMove && M.canMove) return false;

            if (!cloud && M.cloud) return true;
            if (cloud && !M.cloud) return false;

            if (launchersAttackRange - canLandHit < M.launchersAttackRange - M.canLandHit) return true;
            if (launchersAttackRange - canLandHit > M.launchersAttackRange - M.canLandHit) return false;

            if (launchersVisionRange - canLandHit < M.launchersVisionRange - M.canLandHit) return true;
            if (launchersVisionRange - canLandHit > M.launchersVisionRange - M.canLandHit) return false;

            if (canLandHit > M.canLandHit) return true;
            if (canLandHit < M.canLandHit) return false;

            if (minDistToAlly < M.minDistToAlly) return true;
            if (minDistToAlly > M.minDistToAlly) return false;

            if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
            else return minDistanceToEnemy <= M.minDistanceToEnemy;
        }
    }

}
