package fortytwo;

import battlecode.common.*;

public class MicroCarrier {

    final int INF = 1000000;
    static int myRange;
    static int myVisionRange;
    static final int RANGE_EXTENDED_LAUNCHER = 20;
    static final int RANGE_LAUNCHER = 16;

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

    MicroCarrier(){
        this.rc = Robot.rc;
        myRange = rc.getType().actionRadiusSquared;
        myVisionRange = rc.getType().visionRadiusSquared;
    }

    static double currentActionRadius;
    static MapLocation currentLoc;

    boolean flee(){
        int oldCooldown = rc.getMovementCooldownTurns();
        boolean didMicro = false;
        while (rc.isMovementReady()){
            if (doMicro(true)) didMicro = true;
            int newCooldown = rc.getMovementCooldownTurns();
            if (oldCooldown == newCooldown) break;
            oldCooldown = newCooldown;
        }
        return didMicro;
    }

    boolean doMicro(boolean flee){
        try {
            if (!rc.isMovementReady()) return false;
            RobotInfo[] units = Robot.enemies;
            if (units.length == 0) return false;

            MicroInfo[] microInfo = new MicroInfo[9];
            for (int i = 0; i < 9; ++i) microInfo[i] = new MicroInfo(dirs[i]);

            currentActionRadius = RANGE_LAUNCHER;
            if (flee) currentActionRadius = RANGE_EXTENDED_LAUNCHER;

            for (RobotInfo unit : units) {
                if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING) break;
                if (unit.getType() != RobotType.LAUNCHER) continue; //TODO: maybe add carriers
                currentLoc = unit.getLocation();
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

            MicroInfo bestMicro = microInfo[8];
            if (flee){
                for (int i = 0; i < 8; ++i) {
                    if (microInfo[i].isBetterForFleeing(bestMicro)) bestMicro = microInfo[i];
                }
            }
            else {
                for (int i = 0; i < 8; ++i) {
                    if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
                }
            }

            if (bestMicro.dir == Direction.CENTER) return true;

            if (rc.canMove(bestMicro.dir)) {
                Robot.move(bestMicro.dir);
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

        int launchersTargeting = 0;
        boolean canMove = true;

        public MicroInfo(Direction dir){
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
            else{
                minDistanceToEnemy = Constants.INF;
            }
        }

        void updateEnemy(){
            if (!canMove) return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist < minDistanceToEnemy)  minDistanceToEnemy = dist;
            if (dist <= currentActionRadius) ++launchersTargeting;
        }

        int safe(){
            if (!canMove) return -1;
            return 1;
        }

        boolean inRange(){
            return minDistanceToEnemy <= myRange;
        }

        //equal => true
        boolean isBetter(MicroInfo M){

            if (safe() > M.safe()) return true;
            if (safe() < M.safe()) return false;

            if (inRange() && !M.inRange()) return true;
            if (!inRange() && M.inRange()) return false;

            if (launchersTargeting < M.launchersTargeting) return true;
            if (M.launchersTargeting < launchersTargeting) return false;

            if (inRange()){
                return minDistanceToEnemy >= M.minDistanceToEnemy;
            }
            if(dir == Direction.CENTER) return true;
            if (M.dir == Direction.CENTER) return false;
            return minDistanceToEnemy <= M.minDistanceToEnemy;
        }

        boolean isBetterForFleeing(MicroInfo M){
            if (safe() > M.safe()) return true;
            if (safe() < M.safe()) return false;

            if (launchersTargeting < M.launchersTargeting) return true;
            if (M.launchersTargeting < launchersTargeting) return false;

            return minDistanceToEnemy > M.minDistanceToEnemy;
        }
    }

}
