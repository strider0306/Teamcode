package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.myroadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.opencvpipelines.AprilTagDetectionPipeline;
import org.firstinspires.ftc.teamcode.powerplayV2.AprilTagDetectionSubsystem;
import org.firstinspires.ftc.teamcode.powerplayV2.PowerPlayRobotV2;
import org.firstinspires.ftc.teamcode.powerplayV2.RoadRunnerSubsystem;
import org.firstinspires.ftc.teamcode.powerplayV2.commands.AutonomousCommandWOPassThrough;
import org.firstinspires.ftc.teamcode.powerplayV2.subsystems.ArmSubsystem;
import org.firstinspires.ftc.teamcode.powerplayV2.subsystems.BasketSubsystem;
import org.firstinspires.ftc.teamcode.powerplayV2.subsystems.ClawSubsystem;
import org.firstinspires.ftc.teamcode.powerplayV2.subsystems.ElevatorSubsystem;
import org.firstinspires.ftc.teamcode.robotbase.GamepadExEx;

@Autonomous(name = "Season: PP Plan A_NoOtherSide(parking)")
public class PowerPlayAutonomousPlanA_NoOtherSide_OnlyParking extends CommandOpMode {

    PowerPlayRobotV2 robot;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;
    protected ElapsedTime runtime;
    protected SampleMecanumDrive drive;
    protected RoadRunnerSubsystem RR;
    protected AprilTagDetectionSubsystem april_tag;

    @Override
    public void initialize() {
        GamepadExEx driverOp = new GamepadExEx(gamepad1);
        GamepadExEx toolOp = new GamepadExEx(gamepad2);

        robot = new PowerPlayRobotV2(hardwareMap, telemetry, driverOp, toolOp);

        drive = new SampleMecanumDrive(hardwareMap);

        RR = new RoadRunnerSubsystem(drive, hardwareMap, false);

        april_tag = new AprilTagDetectionSubsystem(robot.camera);

        runtime = new ElapsedTime();
    }

    public void waitForStart() {
        /////////////////////////////////// Recognizing the Tag ///////////////////////////////////
        /*
         * The INIT-loop:
         * This REPLACES waitForStart!
         */
        while (!isStarted() && !isStopRequested()) {
            april_tag.aprilTagCheck();
            sleep(20);
        }
    }

    @Override
    public void run() {
        super.run();
        // TODO: Make telemetry subsystem/command and remove this function
        robot.telemetryUpdate();
        robot.dashboardTelemetryUpdate();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initialize();
        waitForStart();

        ///////////////////////////////// Running the Trajectories /////////////////////////////////

        int i = 0;

        if (isStopRequested()) return;

        RR.runHS();

        if (april_tag.getTagOfInterest().id == april_tag.LEFT) RR.runP1();
        else if (april_tag.getTagOfInterest().id == april_tag.RIGHT|| april_tag.getTagOfInterest() == null) RR.runP3();
        else RR.runTOMID();

        // run the scheduler
        while (!isStopRequested() && opModeIsActive()) {
            run();
        }
        reset();
    }
}