package third;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

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
        this.otherComm = new OtherComm(); //should go after explore
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
        otherComm.checkSymmetry();
    }

}