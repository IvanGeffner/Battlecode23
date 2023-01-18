package first;
import battlecode.common.*;

public abstract class Robot {

    static RobotController rc;
    static HeadquarterComm hComm;

    OtherComm otherComm;

    Pathfinder pathfinder;

    Direction[] directions = Direction.values();
    Explore explore;




    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc; //this should always go first
        this.hComm = new HeadquarterComm();
        this.pathfinder = new Pathfinder();
        this.explore = new Explore();
        this.otherComm = new OtherComm();
    }

    abstract void play() throws GameActionException;

    void initTurn(){
    }

    void endTurn() throws GameActionException {
        pathfinder.bugPath.checkCurrent();
        explore.mapdata.run();
        explore.mapdata.reportMap();
        if (rc.getType() == RobotType.CARRIER){
            Carrier c = (Carrier)this;
            c.im.run();
        }
    }

}