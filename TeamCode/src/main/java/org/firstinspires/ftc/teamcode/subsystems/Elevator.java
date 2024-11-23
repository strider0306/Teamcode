package org.firstinspires.ftc.teamcode.subsystems;


import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Elevator extends SubsystemBase {
    private Telemetry telemetry;
    private DcMotorEx motor;
    private TouchSensor bottomLimit;
    private boolean extending;
    private boolean isHomed;
    private int extendingPosition;
    private boolean retracting;
    private int retractingPosition;

    private double wormAngle;


    public Elevator(HardwareMap hm, Telemetry tm){
        wormAngle = 0; //the value of the worm angle so we can calculate max distance
        motor = hm.get(DcMotorEx.class, "Extend");
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bottomLimit = hm.get(TouchSensor.class, "Touch");
        telemetry = tm;
    }

    public void SetWormAngle(double angle) {
        wormAngle = angle;
    }

    public void extend(){
        extend(0.5);
    }


    public void extend(double whatPower) {
        telemetry.addData("ElevatorState", "extend");
//        if (isExtended()){
//            brake();
//        } else {
            extending = true;
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motor.setPower(whatPower);
//        }
    }

//    public void extend(int position) {
//        extendingPosition = position;
//        extend(0.5);
//    }

    public void retract(){
        retract(-0.5);
    }

    public void retract(double whatPower) {
        telemetry.addData("ElevatorState", "extend");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setPower(whatPower);

        retracting = true;

        if (isRetracted()){
            brake();
        } else {
            extending = true;
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motor.setPower(whatPower);
        }
    }

//    public void setPower(double whatPower) {
//        if (whatPower<0) {
//            retract(whatPower);
//        } else if (whatPower>0) {
//            extend(whatPower);
//        } else {
//            brake();
//        }
//    }

    public void brake(){
        telemetry.addData("ElevatorState", "stop");
        motor.setPower(0);
    }

    public double getDistance(){
        return motor.getCurrentPosition();
    }

    public boolean isRetracted(){
        return bottomLimit.isPressed();
    }

    //this method returns true if we are maxed out on distance
    public boolean isExtended(){
        //TODO: calculate maximum distance based on angle



        return getDistance() >= 3300 || getDistance() == extendingPosition;
    }

    //this function fires every cycle, at about 50hz, so anything in here will effectively be the default state
    @Override
    public void periodic() {
        telemetry.addData("ElevatorIsRetracted", isRetracted());
        telemetry.addData("ElevatorIsExtended", isExtended());
        telemetry.addData("ElevatorDistance", getDistance());
        telemetry.addData("IsHomed", isHomed);

//        if (bottomLimit.isPressed() && !extending && !retracting) {
//            isHomed = true;
//            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        }
//
//        if (bottomLimit.isPressed() && retracting) {
//            retracting = false;
//            retractingPosition = Integer.MIN_VALUE;
//            brake();
//        }
//
//        if (isExtended() && extending) {
//            extending = false;
//            extendingPosition = Integer.MIN_VALUE;
//            brake();
//        }
    }
}