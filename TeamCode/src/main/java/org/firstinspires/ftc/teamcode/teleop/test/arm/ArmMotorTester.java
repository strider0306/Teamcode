package org.firstinspires.ftc.teamcode.teleop.test.arm;

import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.common.HardwareDrive;
import org.firstinspires.ftc.teamcode.common.Button;
import org.firstinspires.ftc.teamcode.common.kinematics.ArmKinematics;
import org.firstinspires.ftc.teamcode.common.constantsPKG.Constants;

@TeleOp(name = "Arm Test Move", group = "Drive")

public class ArmMotorTester extends OpMode{
    //local class objects
    HardwareDrive robot = new HardwareDrive();
    ArmKinematics armKinematics = new ArmKinematics();
    Constants constants = new Constants();

    //buttons
    Button x = new Button();
    Button y = new Button();
    Button a = new Button();
    Button b = new Button();

    ElapsedTime resetTimer = new ElapsedTime();
    View relativeLayout;

    private double power = 0.5;

    @Override
    public void init(){
        robot.init(hardwareMap);

        telemetry.addData("Say", "Hello Driver");
    }

    @Override
    public void init_loop() { //Loop between "init" and "start"
        //  robot.setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // robot.setRunMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.armBase.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.armTop.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        robot.armBase.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.armTop.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


    }

    @Override
    public void start(){
    }

    @Override
    public void loop() { //Loop between "start" and "stop"
        UpdatePlayer1();
        UpdateButton();
        UpdateTelemetry();
    }

    private void UpdateTelemetry() {
        telemetry.addData("Top", robot.armTop.getCurrentPosition());
        telemetry.addData("Bottom", robot.armBase.getCurrentPosition());
        telemetry.addData("Power", power);
        telemetry.update();
    }

    private void UpdateButton() {
        x.update(gamepad1.x);
        y.update(gamepad1.y);
        a.update(gamepad1.a);
        b.update(gamepad1.b);
    }

    void UpdatePlayer1(){
        setArmPower();
    }

    public void setTargetPositive(){
        int baseCurrent = robot.armBase.getCurrentPosition();
        int topCurrent = robot.armTop.getCurrentPosition();

      //  robot.armBase.setTargetPosition(baseCurrent + 100);
        robot.armTop.setTargetPosition(topCurrent + 10);

     //   robot.armBase.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        robot.armTop.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);

      //  robot.armBase.setPower(0.3);
        robot.armTop.setPower(power);
    }

    public void setTargetNegative(){
        int baseDegree = robot.armBase.getCurrentPosition();
        int topCurrent = robot.armTop.getCurrentPosition();

       // robot.armBase.setTargetPosition(baseCurrent - 100);
        robot.armTop.setTargetPosition(topCurrent - 10);

      //  robot.armBase.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        robot.armTop.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);

    //    robot.armBase.setPower(0.3);
        robot.armTop.setPower(power);
    }

    public void maintainHeightToGroundPositive(){
        double baseDegree = robot.armBase.getCurrentPosition() * constants.CLICKS_PER_BASE_REV;
        double topDegree = robot.armTop.getCurrentPosition() * constants.CLICKS_PER_TOP_REV;


            robot.armBase.setTargetPosition((int)((baseDegree * constants.DEGS_PER_BASE_CLICK) + (10 / constants.RATIO_CLICKS)));
            robot.armTop.setTargetPosition((int)(topDegree * constants.DEGS_PER_TOP_CLICK) - 10);

            robot.armBase.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
            robot.armBase.setPower(power);

            robot.armTop.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
            robot.armTop.setPower(power);
    }

    public void maintainHeightToGroundNegative(){
        double baseDegree = robot.armBase.getCurrentPosition() * constants.CLICKS_PER_BASE_REV;
        double topDegree = robot.armTop.getCurrentPosition() * constants.CLICKS_PER_TOP_REV;

        robot.armBase.setTargetPosition((int)((baseDegree * constants.DEGS_PER_BASE_CLICK) - (10 / constants.RATIO_CLICKS)));
        robot.armTop.setTargetPosition((int)(topDegree * constants.DEGS_PER_TOP_CLICK) + 10);

        robot.armBase.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        robot.armBase.setPower(power);

        robot.armTop.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        robot.armTop.setPower(power);
    }

    void setArmPower(){
        if (x.getState() == Button.State.HELD){
            setTargetPositive();
        } else if (y.getState() == Button.State.HELD){
            setTargetNegative();
        } else if (x.getState() == Button.State.TAP){
            robot.armTop.setPower(0);
            robot.armBase.setPower(0);
        }

        if (a.getState() == Button.State.HELD){
            maintainHeightToGroundPositive();
        } else if (b.getState() == Button.State.HELD){
            maintainHeightToGroundNegative();
        } else if (x.getState() == Button.State.TAP){
            robot.armTop.setPower(0);
            robot.armBase.setPower(0);
        }

        if (a.getState() == Button.State.TAP){
            power += 0.1;
        } else if (b.getState() == Button.State.TAP){
            power -= 0.1;
        }
        if (power < 0) power = 0;
        else if (power > 1) power = 1;
    }

    @Override
    public void stop() {
    }
}
