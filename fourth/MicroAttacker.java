package fourth;

import battlecode.common.*;

public class MicroAttacker {

    final int INF = 1000000;
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    static int myRange;
    static int myVisionRange;

    static int myDMG = RobotType.LAUNCHER.damage; //TODO

    static final int RANGE_EXTENDED_LAUNCHER = 20;
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
            RobotInfo[] units = rc.senseNearbyRobots(myVisionRange, rc.getTeam().opponent());
            if (units.length == 0) return false;
            canAttack = rc.isActionReady();

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

            if (!shouldPlaySafe) return false;

            alwaysInRange = false;
            if (!rc.isActionReady()) alwaysInRange = true;

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

            units = rc.senseNearbyRobots(8, rc.getTeam());
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

            if (bestMicro.dir == Direction.CENTER) return true;

            if (rc.canMove(bestMicro.dir)) {
                rc.move(bestMicro.dir);
                return true;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    class MicroInfo{
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;
        double DMGreceived = 0;

        double dmgDone = 0;
        double enemiesTargeting = 0;
        double alliesTargeting = 0;
        boolean canMove = true;

        public MicroInfo(Direction dir){
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            else{
                minDistanceToEnemy = INF;
                alliesTargeting += myDMG;
            }
        }

        void updateEnemy(){
            if (!canMove) return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist < minDistanceToEnemy)  minDistanceToEnemy = dist;
            if (dist <= currentActionRadius) DMGreceived += currentDMG;
            if (dist <= myRange){
                dmgDone = myDMG;
            }
            if (dist <= RANGE_EXTENDED_LAUNCHER) enemiesTargeting += currentDMG; //TODO carriers?
        }

        void updateAlly(){
            if (!canMove) return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist <= 2) alliesTargeting += currentDMG;
        }

        int safe(){
            if (!canMove) return -1;
            if (DMGreceived - dmgDone > 0) return 0;
            if (enemiesTargeting > alliesTargeting) return 1;
            return 2;
        }

        boolean inRange(){
            if (alwaysInRange) return true;
            return minDistanceToEnemy <= myRange;
        }

        //equal => true
        boolean isBetter(MicroInfo M){

            if (safe() > M.safe()) return true;
            if (safe() < M.safe()) return false;

            if (inRange() && !M.inRange()) return true;
            if (!inRange() && M.inRange()) return false;

            if (alliesTargeting > M.alliesTargeting) return true;
            if (alliesTargeting < M.alliesTargeting) return false;

            if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
            else return minDistanceToEnemy <= M.minDistanceToEnemy;
        }
    }

}
