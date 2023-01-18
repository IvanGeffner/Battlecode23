package second;

import battlecode.common.*;

public class OtherComm {

    static final int SOLDIER_CHANNEL = 51;
    static final int SYM_CHANNEL = 50;

    static final int HOR_SYM = 1;
    static final int VER_SYM = 2;
    static final int ROT_SYM = 3;
    static final int HSYMBIT = (1 << 2);
    static final int VSYMBIT = (1 << 3);
    static final int RSYMBIT = (1 << 4);

    static final int BYTECODE_THRESHOLD = 500;

    RobotController rc;
    int[] mapInfo;

    static int symIndex = 0;
    static int l;
    static int W, H;
    static int W1, H1;

    Integer sym = null;
    boolean horizontal = true;
    boolean vertical = true;
    boolean rotational = true;

    OtherComm(){
        this.rc = Robot.rc;
        mapInfo = MapData.mapInfo;
        l = rc.getMapHeight()*rc.getMapWidth();
        W = rc.getMapWidth(); H = rc.getMapHeight();
        W1 = rc.getMapWidth()-1; H1 = rc.getMapHeight()-1;
    }

    void reportSoldier() throws GameActionException {
        int x = getSoldiers();
        rc.writeSharedArray(SOLDIER_CHANNEL, x+1);
    }

    int getSoldiers() throws GameActionException {
        return rc.readSharedArray(SOLDIER_CHANNEL);
    }

    Integer getSymmetry(){
        return sym;
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

    void updateSymmetry() throws GameActionException {
        if (sym != null) return;
        int symCode = rc.readSharedArray(SYM_CHANNEL);
        if ((symCode & HOR_SYM) != 0) eliminateHorizontal();
        if ((symCode & VER_SYM) != 0) eliminateVertical();
        if ((symCode & ROT_SYM) != 0) eliminateRotational();
        int code = 0;
        if (!horizontal) code |= HOR_SYM;
        if (!vertical) code |= VER_SYM;
        if (!rotational) code |= ROT_SYM;
        if (rc.canWriteSharedArray(SYM_CHANNEL, code)) rc.writeSharedArray(SYM_CHANNEL, code);
    }

    void checkSymmetry(){
        if (sym != null) return;
        int x, y;
        int symx, symy;
        int code, newcode;
        int newSymIndex;
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




}
