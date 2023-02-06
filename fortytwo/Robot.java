package fortytwo;

import battlecode.common.*;

public abstract class Robot {

    static RobotController rc;
    static HeadquarterComm hComm;

    static BFSManager bfsManager;

    static OtherComm otherComm;

    static Pathfinder pathfinder;

    static AdjacentTiles adjacentTiles;

    Direction[] directions = Direction.values();
    static Explore explore;

    static VisibleTilesManager vt;

    RobotType myType;
    static int roundBirth;

    static int H, W;

    static String bytecodeDebug = new String();

    static RobotInfo[] enemies;

    static int visionRadiusSquared;
    static Team opponent;

    static MapLocation closestEnemyLauncher = null;
    static int roundSeen = -100;

    static boolean hq = false;
    static boolean launcher = false;

    static final int SMALL = 0, MEDIUM = 1, LARGE = 2;

    static int MAP_TYPE = 1;


    void checkMapSize(){
        if (H*W < 1000) MAP_TYPE = SMALL;
    }

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc; //this should always go first
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        checkMapSize();
        visionRadiusSquared = rc.getType().visionRadiusSquared;
        hq = rc.getType() == RobotType.HEADQUARTERS;
        launcher = rc.getType() == RobotType.LAUNCHER || rc.getType() == RobotType.BOOSTER || rc.getType() == RobotType.DESTABILIZER;
        opponent = rc.getTeam().opponent();
        bytecodeDebug += "Before hcomm " + Clock.getBytecodeNum() + " ";
        this.hComm = new HeadquarterComm();
        bytecodeDebug += "Before pathfinding " + Clock.getBytecodeNum() + " ";
        this.pathfinder = new Pathfinder();
        bytecodeDebug += "Before explore " + Clock.getBytecodeNum() + " ";
        this.explore = new Explore();
        bytecodeDebug += "Before othercomm " + Clock.getBytecodeNum() + " ";
        this.otherComm = new OtherComm(); //should go after explore
        bytecodeDebug += "Before bfsmanager " + Clock.getBytecodeNum() + " ";
        this.bfsManager = new BFSManager();
        this.myType = rc.getType();
        bytecodeDebug += "Before vtm " + Clock.getBytecodeNum() + " ";
        this.vt = new VisibleTilesManager();
        this.roundBirth = rc.getRoundNum();
        bytecodeDebug += "Before adjacentTiles " + Clock.getBytecodeNum() + " ";
        adjacentTiles = new AdjacentTiles();
        bytecodeDebug += "After adjacentTiles " + Clock.getBytecodeNum() + " ";
    }

    abstract void play() throws GameActionException;

    void initTurn() throws GameActionException {
        bytecodeDebug += "Before Adjacents " + Clock.getBytecodeNum() + " ";
        enemies = rc.senseNearbyRobots(visionRadiusSquared, opponent);
        if (!hq) computeClosestEnemyLauncher();
        boolean computedAdjacent = rc.getType() != RobotType.HEADQUARTERS && rc.isMovementReady() && adjacentTiles.reset();
        bytecodeDebug += "After Adjacents " + Clock.getBytecodeNum() + " ";
        if (!computedAdjacent) bfsManager.runBuffer();
        bytecodeDebug += "After buffer " + Clock.getBytecodeNum() + " ";
        otherComm.updateSymmetry();
        otherComm.checkEnemyComms();
        otherComm.reportClosestEnemyLauncher();
        if (rc.getRoundNum() > 400 && rc.getRobotCount() <= 4) rc.resign();
    }

    void endTHQ() throws GameActionException {
        otherComm.checkElixirTrigger();
        explore.mapdata.computeElixir();
        explore.mapdata.updateWell();
        Constants.indicatorString += "BC:" + Clock.getBytecodeNum() + " ";
        otherComm.reportClosestEnemyLauncher();
        explore.mapdata.run();
        explore.mapdata.reportMap();
        otherComm.checkSymmetry();
        otherComm.fillSymmetry();
        rc.setIndicatorString(Constants.indicatorString);
    }

    void endTurn() throws GameActionException {
        if (myType == RobotType.HEADQUARTERS){
            endTHQ();
            return;
        }
        pathfinder.bugPath.fill();
        explore.mapdata.updateWell();
        Constants.indicatorString += "BC:" + Clock.getBytecodeNum() + " ";
        pathfinder.bugPath.checkCurrent();
        otherComm.reportClosestEnemyLauncher();
        explore.mapdata.run();
        explore.mapdata.reportMap();
        if (myType == RobotType.CARRIER){
            vt.run();
        }
        bfsManager.run();
        otherComm.checkSymmetry();
        otherComm.fillSymmetry();
        rc.setIndicatorString(Constants.indicatorString);
    }

    static void move(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER) return;
        rc.move(dir);
        enemies = rc.senseNearbyRobots(visionRadiusSquared, opponent);
        if (!hq) computeClosestEnemyLauncher();
        if (rc.isMovementReady()) adjacentTiles.softReset();
    }

    static void computeClosestEnemyLauncher(){
        boolean first = true;
        for (RobotInfo r :enemies){
            if (r.getType() != RobotType.LAUNCHER) continue;
            if (first || closestEnemyLauncher.distanceSquaredTo(rc.getLocation()) > r.location.distanceSquaredTo(rc.getLocation())){
                closestEnemyLauncher = r.getLocation();
                roundSeen = rc.getRoundNum();
            }
            first = false;
        }
    }

    static void write(int index, int code) throws GameActionException {
        /*if (index >= GameConstants.SHARED_ARRAY_LENGTH - 8){
            if (rc.getType() != RobotType.HEADQUARTERS) throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "IDIOT");
            System.out.println("Writing " + code + " at " + index);
        }*/
        rc.writeSharedArray(index, code);
    }

}