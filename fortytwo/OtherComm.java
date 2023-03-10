package fortytwo;

import battlecode.common.*;

public class OtherComm {

    static final int SOLDIER_CHANNEL = 51;

    static final int ENEMY_CHANNEL = 52;
    static final int AMP_SYM_CHANNEL = 50;

    static final int HOR_SYM = 1;
    static final int VER_SYM = 2;
    static final int ROT_SYM = 3;
    static final int HSYMBIT = (1 << 0);
    static final int VSYMBIT = (1 << 1);
    static final int RSYMBIT = (1 << 2);

    static final int MIN_ROBOTS_ELIXIR = 70;

    static final int BYTECODE_THRESHOLD = 500;
    static final int MAX_BYTECODE_REPORT = 9500;

    RobotController rc;
    int[] mapInfo;

    static int symIndex = 0;

    static int fillIndex = 0;

    static int l;
    static int W, H;
    static int W1, H1;

    Integer sym = null;
    boolean horizontal = true;
    boolean vertical = true;
    boolean rotational = true;

    MapLocation closestEnemyLauncher = null;

    OtherComm(){
        this.rc = Robot.rc;
        mapInfo = MapData.mapInfo;
        W = Robot.W; H = Robot.H;
        l = W*H;
        W1 = W-1; H1 = H-1;
    }

    void reportSoldier(int amt) throws GameActionException {
        int x = getSoldiers();
        Robot.write(SOLDIER_CHANNEL, x+amt);
    }

    int getSoldiers() throws GameActionException {
        return rc.readSharedArray(SOLDIER_CHANNEL);
    }

    void reportAmplifier() throws GameActionException {
        int x = getAmplifiers();
        int code = (rc.readSharedArray(AMP_SYM_CHANNEL) & 7);
        Robot.write(AMP_SYM_CHANNEL, ((x+1) << 3) | code);
    }

    int getAmplifiers() throws GameActionException {
        return (rc.readSharedArray(AMP_SYM_CHANNEL) >>> 3);
    }

    /************************ ENEMIES *****************************/

    //Also returns closest enemy launcher
    void checkEnemyComms() throws GameActionException {
        int trueR = (rc.getRoundNum())&0xF;
        MapLocation ans = null;
        int bestDist = 0;
        MapLocation myLoc = rc.getLocation();
        closestEnemyLauncher = null;

        for (int i = 0; i < Robot.hComm.numBases; ++i){
            int code = rc.readSharedArray(ENEMY_CHANNEL + i);
            if (code == 0) continue;
            int x = (code >>> 6)&0x3F;
            int y = code & 0x3F;
            int r = (code >>> 12);
            int r1 = (r+1)&0xF;

            //check round
            if (r != trueR && r1 != trueR){
                if (rc.canWriteSharedArray(ENEMY_CHANNEL + i, 0)){
                    Robot.write(ENEMY_CHANNEL+i, 0);
                    continue;
                }
            }

            MapLocation enemyLoc = new MapLocation(x,y);
            if (rc.canSenseLocation(enemyLoc)) {
                RobotInfo ri = rc.senseRobotAtLocation(enemyLoc);
                if (ri == null || ri.getType() != RobotType.LAUNCHER || ri.getTeam() != rc.getTeam()) {
                    if (rc.canWriteSharedArray(ENEMY_CHANNEL + i, 0)) {
                        Robot.write(ENEMY_CHANNEL + i, 0);
                    }
                }
                continue;
            }

            int d = myLoc.distanceSquaredTo(enemyLoc);
            if (ans == null || d < bestDist){
                ans = enemyLoc;
                bestDist = d;
            }
        }
        if (ans != null) rc.setIndicatorDot(ans, 0, 255, 255);
        closestEnemyLauncher = ans;
    }

    MapLocation getLauncherLoc(int index) throws GameActionException {
        int code = rc.readSharedArray(ENEMY_CHANNEL + index);
        if (code == 0) return null;
        int x = (code >>> 6)&0x3F;
        int y = code & 0x3F;
        return new MapLocation(x,y);
    }

    void reportClosestEnemyLauncher() throws GameActionException {
        if (!rc.canWriteSharedArray(0,0)) return;
        MapLocation ans = null;
        int bestDist = 0;
        MapLocation myLoc = rc.getLocation();
        RobotInfo[] enemies = Robot.enemies;
        for (RobotInfo r : enemies){
            if (Clock.getBytecodeNum() > BYTECODE_THRESHOLD) break;
            if (r.getType() != RobotType.LAUNCHER) continue;
            int d = myLoc.distanceSquaredTo(r.getLocation());
            if (ans == null || d < bestDist){
                bestDist = d;
                ans = r.getLocation();
            }
        }
        if (ans == null) return;
        int code = ((rc.getRoundNum() & 0xF) << 12) | (ans.x << 6) | ans.y;
        //Constants.indicatorString += "Reporting this guy: " + ans.x + " " + ans.y;
        if (rc.canWriteSharedArray(ENEMY_CHANNEL + Robot.hComm.hqIndex - 1, code)) Robot.write(ENEMY_CHANNEL + Robot.hComm.hqIndex - 1, code);
    }




    /********************** SYM ***************************/

    Integer getSymmetry(){
        return sym;
    }

    MapLocation getSymmetric(MapLocation loc){
        if (sym == null) return null;
        if (sym == ROT_SYM) return new MapLocation(W1 - loc.x, H1 - loc.y);
        else if (sym == VER_SYM) return new MapLocation(loc.x, H1 - loc.y);
        else if (sym == HOR_SYM) return new MapLocation(W1 - loc.x, loc.y);
        return null;
    }

    void eliminateHorizontal(){
        if (!horizontal) return;
        horizontal = false;
        finalCheckSym();
    }

    void eliminateVertical(){
        if (!vertical) return;
        vertical = false;
        finalCheckSym();
    }

    void eliminateRotational(){
        if (!rotational) return;
        rotational = false;
        finalCheckSym();
    }

    void finalCheckSym(){
        if (rotational && !vertical && !horizontal) sym = ROT_SYM;
        if (!rotational && vertical && !horizontal) sym = VER_SYM;
        if (!rotational && !vertical && horizontal) sym = HOR_SYM;
    }

    boolean stopCheckingSym = false;

    void updateSymmetry() throws GameActionException {
        if (stopCheckingSym) return;
        int symCode = rc.readSharedArray(AMP_SYM_CHANNEL);
        if ((symCode & HSYMBIT) != 0) eliminateHorizontal();
        if ((symCode & VSYMBIT) != 0) eliminateVertical();
        if ((symCode & RSYMBIT) != 0) eliminateRotational();
        int code = 0;
        if (!horizontal) code |= HSYMBIT;
        if (!vertical) code |= VSYMBIT;
        if (!rotational) code |= RSYMBIT;
        code |= (symCode & 0xFFF8);
        if (symCode == code && sym != null){
            stopCheckingSym = true;
            return;
        }
        if (rc.canWriteSharedArray(AMP_SYM_CHANNEL, code)) Robot.write(AMP_SYM_CHANNEL, code);
    }

    int x, y;
    int symx, symy;
    int code, newcode;
    int newSymIndex;

    void checkSymmetry(){
        if (sym != null) return;
        for(; true; symIndex = (symIndex + 1)%l){
            if (Clock.getBytecodesLeft() < BYTECODE_THRESHOLD) return;
            if (sym != null) return;
            x = symIndex/H; y = symIndex%H;
            code = (mapInfo[symIndex/4] >>> (4*(symIndex%4)))&0xF;
            if (code == 0) continue;

            //HORIZONTAL
            if (horizontal) {
                symx = W1 - x;
                symy = y;
                newSymIndex = H * symx + symy;
                newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
                if (newcode != 0) {
                    switch (code) {
                        case 1:
                            if (newcode != 5) eliminateHorizontal();
                            break;
                        case 2:
                            if (newcode != 4) eliminateHorizontal();
                            break;
                        case 3:
                            if (newcode != 3) eliminateHorizontal();
                            break;
                        case 4:
                            if (newcode != 2) eliminateHorizontal();
                            break;
                        case 5:
                            if (newcode != 1) eliminateHorizontal();
                            break;
                        case 6:
                            if (newcode != 8) eliminateHorizontal();
                            break;
                        case 7:
                            if (newcode != 7) eliminateHorizontal();
                            break;
                        case 8:
                            if (newcode != 6) eliminateHorizontal();
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            if (newcode != code) eliminateHorizontal();
                            break;
                        default:
                            break;
                    }
                }
            }

            //VERTICAL
            if (vertical) {
                symx = x;
                symy = H1 - y;
                newSymIndex = H * symx + symy;
                newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
                if (newcode != 0) {
                    switch (code) {
                        case 1:
                            if (newcode != 1) eliminateVertical();
                            break;
                        case 2:
                            if (newcode != 8) eliminateVertical();
                            break;
                        case 3:
                            if (newcode != 7) eliminateVertical();
                            break;
                        case 4:
                            if (newcode != 6) eliminateVertical();
                            break;
                        case 5:
                            if (newcode != 5) eliminateVertical();
                            break;
                        case 6:
                            if (newcode != 4) eliminateVertical();
                            break;
                        case 7:
                            if (newcode != 3) eliminateVertical();
                            break;
                        case 8:
                            if (newcode != 2) eliminateVertical();
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            if (newcode != code) eliminateVertical();
                            break;
                        default:
                            break;
                    }
                }
            }

            //ROTATIONAL
            if (rotational) {
                symx = W1 - x;
                symy = H1 - y;
                newSymIndex = H * symx + symy;
                newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
                if (newcode != 0) {
                    switch (code) {
                        case 1:
                            if (newcode != 5) eliminateRotational();
                            break;
                        case 2:
                            if (newcode != 6) eliminateRotational();
                            break;
                        case 3:
                            if (newcode != 7) eliminateRotational();
                            break;
                        case 4:
                            if (newcode != 8) eliminateRotational();
                            break;
                        case 5:
                            if (newcode != 1) eliminateRotational();
                            break;
                        case 6:
                            if (newcode != 2) eliminateRotational();
                            break;
                        case 7:
                            if (newcode != 3) eliminateRotational();
                            break;
                        case 8:
                            if (newcode != 4) eliminateRotational();
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            if (newcode != code) eliminateRotational();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    void fillSymmetry(){
        if (sym == null) return;
        for(; true; fillIndex = (fillIndex + 1)%l){
            if (Clock.getBytecodesLeft() < BYTECODE_THRESHOLD) return;
            x = fillIndex/H; y = fillIndex%H;
            code = (mapInfo[fillIndex/4] >>> (4*(fillIndex%4)))&0xF;
            if (code != 0) continue;

            //HORIZONTAL
            if (sym == HOR_SYM) {
                symx = W1 - x;
                symy = y;
                newSymIndex = H * symx + symy;
                newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
                if (newcode != 0) {
                    int vMult = (1 << (4*(fillIndex%4)));
                    switch (newcode) {
                        case 1:
                            mapInfo[fillIndex/4] |= 5*vMult;
                            break;
                        case 2:
                            mapInfo[fillIndex/4] |= 4*vMult;
                            break;
                        case 3:
                            mapInfo[fillIndex/4] |= 3*vMult;
                            break;
                        case 4:
                            mapInfo[fillIndex/4] |= 2*vMult;
                            break;
                        case 5:
                            mapInfo[fillIndex/4] |= 1*vMult;
                            break;
                        case 6:
                            mapInfo[fillIndex/4] |= 8*vMult;
                            break;
                        case 7:
                            mapInfo[fillIndex/4] |= 7*vMult;
                            break;
                        case 8:
                            mapInfo[fillIndex/4] |= 6*vMult;
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            mapInfo[fillIndex/4] |= newcode*vMult;
                            break;
                        default:
                            break;
                    }
                    Robot.explore.mapdata.update(fillIndex, newcode);
                }
            }

            //VERTICAL
            if (sym == VER_SYM) {
                symx = x;
                symy = H1 - y;
                newSymIndex = H * symx + symy;
                newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
                if (newcode != 0) {
                    int vMult = (1 << (4*(fillIndex%4)));
                    switch (newcode) {
                        case 1:
                            mapInfo[fillIndex/4] |= 1*vMult;
                            break;
                        case 2:
                            mapInfo[fillIndex/4] |= 8*vMult;
                            break;
                        case 3:
                            mapInfo[fillIndex/4] |= 7*vMult;
                            break;
                        case 4:
                            mapInfo[fillIndex/4] |= 6*vMult;
                            break;
                        case 5:
                            mapInfo[fillIndex/4] |= 5*vMult;
                            break;
                        case 6:
                            mapInfo[fillIndex/4] |= 4*vMult;
                            break;
                        case 7:
                            mapInfo[fillIndex/4] |= 3*vMult;
                            break;
                        case 8:
                            mapInfo[fillIndex/4] |= 2*vMult;
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            mapInfo[fillIndex/4] |= newcode*vMult;
                            break;
                        default:
                            break;
                    }
                    Robot.explore.mapdata.update(fillIndex, newcode);
                }
            }

            //ROTATIONAL
            if (sym == ROT_SYM) {
                symx = W1 - x;
                symy = H1 - y;
                newSymIndex = H * symx + symy;
                newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
                if (newcode != 0) {
                    int vMult = (1 << (4*(fillIndex%4)));
                    switch (newcode) {
                        case 1:
                            mapInfo[fillIndex/4] |= 5*vMult;
                            break;
                        case 2:
                            mapInfo[fillIndex/4] |= 6*vMult;
                            break;
                        case 3:
                            mapInfo[fillIndex/4] |= 7*vMult;
                            break;
                        case 4:
                            mapInfo[fillIndex/4] |= 8*vMult;
                            break;
                        case 5:
                            mapInfo[fillIndex/4] |= 1*vMult;
                            break;
                        case 6:
                            mapInfo[fillIndex/4] |= 2*vMult;
                            break;
                        case 7:
                            mapInfo[fillIndex/4] |= 3*vMult;
                            break;
                        case 8:
                            mapInfo[fillIndex/4] |= 4*vMult;
                            break;
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                            mapInfo[fillIndex/4] |= newcode*vMult;
                            break;
                        default:
                            break;
                    }
                    Robot.explore.mapdata.update(fillIndex, newcode);
                }
            }
        }
    }

    void checkSyms(MapLocation loc, int code){
        //rc.setIndicatorDot(loc, 100, 100, 100);
        x = loc.x; y = loc.y;
        if (horizontal) {
            symx = W1 - x;
            symy = y;
            int newSymIndex = H * symx + symy;
            int newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
            if (newcode != 0) {
                switch (code) {
                    case 1:
                        if (newcode != 5) eliminateHorizontal();
                        break;
                    case 2:
                        if (newcode != 4) eliminateHorizontal();
                        break;
                    case 3:
                        if (newcode != 3) eliminateHorizontal();
                        break;
                    case 4:
                        if (newcode != 2) eliminateHorizontal();
                        break;
                    case 5:
                        if (newcode != 1) eliminateHorizontal();
                        break;
                    case 6:
                        if (newcode != 8) eliminateHorizontal();
                        break;
                    case 7:
                        if (newcode != 7) eliminateHorizontal();
                        break;
                    case 8:
                        if (newcode != 6) eliminateHorizontal();
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        if (newcode != code) eliminateHorizontal();
                        break;
                    default:
                        break;
                }
            }
        }

        //VERTICAL
        if (vertical) {
            symx = x;
            symy = H1 - y;
            newSymIndex = H * symx + symy;
            newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
            if (newcode != 0) {
                switch (code) {
                    case 1:
                        if (newcode != 1) eliminateVertical();
                        break;
                    case 2:
                        if (newcode != 8) eliminateVertical();
                        break;
                    case 3:
                        if (newcode != 7) eliminateVertical();
                        break;
                    case 4:
                        if (newcode != 6) eliminateVertical();
                        break;
                    case 5:
                        if (newcode != 5) eliminateVertical();
                        break;
                    case 6:
                        if (newcode != 4) eliminateVertical();
                        break;
                    case 7:
                        if (newcode != 3) eliminateVertical();
                        break;
                    case 8:
                        if (newcode != 2) eliminateVertical();
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        if (newcode != code) eliminateVertical();
                        break;
                    default:
                        break;
                }
            }
        }

        //ROTATIONAL
        if (rotational) {
            symx = W1 - x;
            symy = H1 - y;
            newSymIndex = H * symx + symy;
            newcode = (mapInfo[newSymIndex / 4] >>> (4 * (newSymIndex % 4))) & 0xF;
            if (newcode != 0) {
                switch (code) {
                    case 1:
                        if (newcode != 5) eliminateRotational();
                        break;
                    case 2:
                        if (newcode != 6) eliminateRotational();
                        break;
                    case 3:
                        if (newcode != 7) eliminateRotational();
                        break;
                    case 4:
                        if (newcode != 8) eliminateRotational();
                        break;
                    case 5:
                        if (newcode != 1) eliminateRotational();
                        break;
                    case 6:
                        if (newcode != 2) eliminateRotational();
                        break;
                    case 7:
                        if (newcode != 3) eliminateRotational();
                        break;
                    case 8:
                        if (newcode != 4) eliminateRotational();
                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                        if (newcode != code) eliminateRotational();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    void checkElixirTrigger() throws GameActionException {
        if (rc.getRobotCount() >= MIN_ROBOTS_ELIXIR && MapData.manaIndex > 1){
            rc.writeSharedArray(MapData.ELIXIR_TRIGGER_CHANNEL, 1);
        }
    }
}
