package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

/**
 * This is NOT an opmode.
 *
 * This class can be used to define all the specific hardware for a single robot.
 * See PushbotTeleopTank_Iterative and others classes starting with "Pushbot" for usage examples.
 * This class builds on the concept of a hardware class by containing common methods (mostly involving motor action) so that they can be used without rewriting them in
 * new code or using lengthy class calls.
 *
 * This class assumes the following device names have been configured on the robot:
 *
 * four motors for Mecanum Drive named motorBackLeft, motorFrontLeft, motorFrontRight, motorBackRight
 * the internal hub IMU named imu
 * a webcam attached to USB.
 */
public class chrisBot
{
    private boolean shooterExists = false, intakeExists = false, webcamExists = false;

    /** MOTOR OBJECTS */
    public DcMotor  motorBackLeft   = null;
    public DcMotor  motorFrontLeft  = null;
    public DcMotor  motorFrontRight  = null;
    public DcMotor  motorBackRight  = null;
    public DcMotor motorIntake = null;
    public DcMotor motorShooter = null;

    public WebcamName webcam = null;

    public static final double COUNTS_PER_MOTOR_REV    = 1120 ;    // eg: TETRIX Motor Encoder
    public static final double DRIVE_GEAR_REDUCTION    = 0.625 ;     // This is < 1.0 if geared UP
    public static final double WHEEL_DIAMETER_INCHES   = 2.95276 ;     // For figuring circumference
    public static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * 3.1415);
    public static final double DRIVE_SPEED = 0.7;
    public static final double TURN_SPEED = 0.3;

    int FLTarget = 0;
    int FRTarget = 0;
    int BLTarget = 0;
    int BRTarget = 0;

    /** GYRO OBJECTS */

    public BNO055IMU imu = null;

    // State used for updating telemetry
    Orientation angles;
    Acceleration gravity;

    /** VUFORIA OBJECTS */

    // IMPORTANT: If you are using a USB WebCam, you must select CAMERA_CHOICE = BACK; and PHONE_IS_PORTRAIT = false;
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;
    private static final boolean PHONE_IS_PORTRAIT = false  ;

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField  = 36 * mmPerInch;

    // private boolean targetVisible = false;
    private float phoneXRotate    = 0;
    private float phoneYRotate    = 0;
    private float phoneZRotate    = 0;

    List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();

    private OpenGLMatrix lastLocation = null;

    /*
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    public VuforiaLocalizer vuforia;

    /*
     * {@link #tfod} is the variable we will use to store our instance of the TensorFlow Object
     * Detection engine.
     */
    public TFObjectDetector tfod;

    public static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    public static final String LABEL_FIRST_ELEMENT = "Quad";
    public static final String LABEL_SECOND_ELEMENT = "Single";

    public static final String VUFORIA_KEY = "AQU7a8H/////AAABmfH4ZcQHIkPTjsjCf80CSVReJtuQBMiQodPHMSkdFHY8RhKT4fIEcY3JbCWjXRsUBFiewYx5etup17dUnX/SIQx6cjctrioEXrID+gV4tD9B29eCOdFVgyAr+7ZnEHHDYcSnt2pfzDZyMpi+I3IODqbUgVO82UiaZViuZBnA3dNvokZNFwZvv8/YDkcd4LhHv75QdkqgBzKe/TumwxjR/EqtR2fQRy9WnRjNVR9fYGl9MsuGNBSEmmys6GczXn8yZ/k2PKusiYz7h4hFGiXmlVLyikZuB4dxETGoqz+WWYUFJAdHzFiBptg5xXaa86qMBYBi3ht0RUiBKicLJhQZzLG0bIEJZWr198ihexUuhhGV";

    /** local OpMode members. */
    HardwareMap hwMap           =  null;
    private ElapsedTime period  = new ElapsedTime();
    private ElapsedTime runtime = new ElapsedTime();

    /* Constructor */
    public chrisBot() { }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        /** Vuforia section */

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        /** Drive motor section */

        // Define and Initialize Motors
        motorBackLeft  = hwMap.get(DcMotor.class, "motorBackLeft");
        motorFrontLeft = hwMap.get(DcMotor.class, "motorFrontLeft");
        motorFrontRight = hwMap.get(DcMotor.class, "motorFrontRight");
        motorBackRight = hwMap.get(DcMotor.class, "motorBackRight");

        imu = hwMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        motorBackLeft.setDirection(DcMotor.Direction.REVERSE); // Set to REVERSE if using AndyMark motors
        motorFrontLeft.setDirection(DcMotor.Direction.REVERSE); // Set to REVERSE if using AndyMark motors
        motorFrontRight.setDirection(DcMotor.Direction.FORWARD); // Set to REVERSE if using AndyMark motors
        motorBackRight.setDirection(DcMotor.Direction.FORWARD); // Set to REVERSE if using AndyMark motors

        // Set all motors to zero power
        motorBackLeft.setPower(0);
        motorFrontLeft.setPower(0);
        motorFrontRight.setPower(0);
        motorBackLeft.setPower(0);

        motorBackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Set all motors to run with encoders.
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        /** Attachment section */

        intakeExists = hwMap.tryGet(DcMotor.class, "motorIntake") != null;
        shooterExists = hwMap.tryGet(DcMotor.class, "motorShooter") != null;
        webcamExists = hwMap.get(WebcamName.class, "Webcam 1") != null;

        if (intakeExists) {
            motorIntake = hwMap.get(DcMotor.class, "motorIntake");
            motorIntake.setDirection(DcMotor.Direction.FORWARD);
            motorIntake.setPower(0);
            motorIntake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motorIntake.setMode((DcMotor.RunMode.RUN_USING_ENCODER));
        }

        if (shooterExists) {
            motorShooter = hwMap.get(DcMotor.class, "motorShooter");
            motorShooter.setDirection(DcMotor.Direction.FORWARD);
            motorShooter.setPower(0);
            motorShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motorShooter.setMode((DcMotor.RunMode.RUN_USING_ENCODER));
        }

        if (webcamExists) {
            webcam = hwMap.get(WebcamName.class, "Webcam 1");
        }


    }

    /** MOTOR METHODS */
    // This method returns true if any drive motor is busy and false otherwise.
    public boolean isBusy() {
        return motorFrontLeft.isBusy() || motorBackRight.isBusy() || motorFrontRight.isBusy() || motorBackLeft.isBusy();
    }
    // This method sets the powers of all the drive motors on the robot.
    public void setAllPower(double speed) {
        motorFrontLeft.setPower(speed);
        motorFrontRight.setPower(speed);
        motorBackRight.setPower(speed);
        motorBackLeft.setPower(speed);
    }
    // This method sets the encoder drive targets on the actual motors.
    public void setTargets() {
        motorFrontLeft.setTargetPosition(FLTarget);
        motorFrontRight.setTargetPosition(FRTarget);
        motorBackLeft.setTargetPosition(BLTarget);
        motorBackRight.setTargetPosition(BRTarget);
    }
    // This method sets the modes of all the motors.
    public void setAllMode(DcMotor.RunMode mode) {
        motorFrontLeft.setMode(mode);
        motorFrontRight.setMode(mode);
        motorBackLeft.setMode(mode);
        motorBackRight.setMode(mode);
    }
    // This method resets the encoder driving targets.
    private void resetTargets() {
        FLTarget = 0;
        FRTarget = 0;
        BLTarget = 0;
        BRTarget = 0;
    }

    // This method checks power levels to be safe and pushes them to the motors
    public void setPower(double flPower, double frPower, double blPower, double brPower)
    {
        double[] powers = {flPower, frPower, blPower, brPower};
        // Check deadzones
        for (int i = 0; i < powers.length; i++) {
            if(powers[i] > 1) {
                powers[i] = 1;
            }
            else if(powers[i] < -1) {
                powers[i] = -1;
            }
        }
        // Push powers
        motorFrontLeft.setPower(powers[0]);
        motorFrontRight.setPower(powers[1]);
        motorBackLeft.setPower(powers[2]);
        motorBackRight.setPower(powers[3]);
    }

    /** DRIVING METHODS */

    // This overloaded method allows the robot to drive back and forward. It can be called with inches and with or without a drive speed.
    public void encoderDrive(double speed, double inches) {
        resetTargets();

        // Determine new target position, and pass to motor controller
        int countsToTravel = (int)(inches * COUNTS_PER_INCH);
        FLTarget = motorFrontLeft.getCurrentPosition() + countsToTravel;
        FRTarget = motorFrontRight.getCurrentPosition() + countsToTravel;
        BLTarget = motorBackLeft.getCurrentPosition() + countsToTravel;
        BRTarget = motorBackRight.getCurrentPosition() + countsToTravel;

        setTargets();

        // Turn On RUN_TO_POSITION
        setAllMode(DcMotor.RunMode.RUN_TO_POSITION);

        // reset the timeout time and start motion.
        runtime.reset();
        setAllPower(testPlatformHardware.DRIVE_SPEED);

        // keep looping while we are still active, and there is time left and motors are running.
        while (isBusy()) {
        }

        // Stop all motion;
        setAllPower(0);

        // Turn off RUN_TO_POSITION
        setAllMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    public void encoderDrive(double inches) {
        encoderDrive(DRIVE_SPEED, inches);
    }

    // This overloaded method allows the robot to drive with encoders on a per wheel basis. It can be called with inches and with or without a drive speed.
    // This method can be used to strafe if used in combination with calculateInches().
    public void wheelMecanumDrive(double[] inches, double timeoutS) {
        resetTargets();

        // Calculate the maximum number of inches any wheel is asked to drive
        double inchesMax = 0;
        for (double inch : inches) {
            if (inch > inchesMax) {
                inchesMax = inch;
            }
        }

        // Determine new target position, and pass to motor controller
        FLTarget = motorFrontLeft.getCurrentPosition() + (int) (inches[0] * testPlatformHardware.COUNTS_PER_INCH);
        FRTarget = motorFrontRight.getCurrentPosition() + (int) (inches[1] * testPlatformHardware.COUNTS_PER_INCH);
        BLTarget = motorBackLeft.getCurrentPosition() + (int) (inches[2] * testPlatformHardware.COUNTS_PER_INCH);
        BRTarget = motorBackRight.getCurrentPosition() + (int) (inches[3] * testPlatformHardware.COUNTS_PER_INCH);

        setTargets();

        // Turn On RUN_TO_POSITION
        setAllMode(DcMotor.RunMode.RUN_TO_POSITION);

        // reset the timeout time and start motion.
        runtime.reset();
        setAllPower(testPlatformHardware.DRIVE_SPEED);

        // keep looping while we are still active, and there is time left and motors are running.
        while ((runtime.seconds() < timeoutS) && isBusy()) {
        }

        // Stop all motion;
        setAllPower(0);

        // Turn off RUN_TO_POSITION
        setAllMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }
    public void wheelMecanumDrive(double[] inches) {
        wheelMecanumDrive(inches, DRIVE_SPEED);
    }

    /** ATTACHMENT METHODS */

    // This method runs the motors in order to drop the Wobble Goal.
    public void dropGoal() {
        /* Code to drop goal goes here */
    }

    // This code runs the motors to shoot exactly one ring.
    public void shootOn() {
        if(shooterExists) {
            motorShooter.setPower(1);
        }
    }
    public void shootOff() {
        if(shooterExists) {
            motorShooter.setPower(0);
        }
    }

    // These methods turn the intake motor on and off, at a set power or at full power.
    public void intakeOn(double power) {
        motorIntake.setPower(power);
    }

    public void intakeOn() {
        motorIntake.setPower(1);
    }

    public void intakeOff() {
        motorIntake.setPower(0);
    }

    /** SENSOR METHODS */
    public Orientation getOrientation() {
        // Code to get the gyro orientation goes here
        return new Orientation();
    }

    /** VUFORIA METHODS */

    // This initializes Vuforia for use.
    public void initVuforia() {
        // Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = chrisBot.VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    // This initializes the TensorFlow object detection (TFOD) engine.
    public void initTfod() {
        int tfodMonitorViewId = hwMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hwMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(chrisBot.TFOD_MODEL_ASSET, chrisBot.LABEL_FIRST_ELEMENT, chrisBot.LABEL_SECOND_ELEMENT);
    }
    public void initVuMarks() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         * We can pass Vuforia the handle to a camera preview resource (on the RC phone);
         * If no camera monitor is desired, use the parameter-less constructor instead (commented out below).
         */
        int cameraMonitorViewId = hwMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hwMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = chrisBot.VUFORIA_KEY;

        /*
          We also indicate which camera on the RC we wish to use.
         */
        parameters.cameraName = webcam;

        // Make sure extended tracking is disabled for this example.
        parameters.useExtendedTracking = false;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        VuforiaTrackables targetsUltimateGoal = vuforia.loadTrackablesFromAsset("UltimateGoal");

        VuforiaTrackable blueTowerGoalTarget = targetsUltimateGoal.get(0);

        VuforiaTrackable redTowerGoalTarget = targetsUltimateGoal.get(1);

        VuforiaTrackable redAllianceTarget = targetsUltimateGoal.get(2);

        VuforiaTrackable blueAllianceTarget = targetsUltimateGoal.get(3);

        VuforiaTrackable frontWallTarget = targetsUltimateGoal.get(4);
        //Set the position of the perimeter targets with relation to origin (center of field)
        redAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        blueAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
        frontWallTarget.setLocation(OpenGLMatrix
                .translation(-halfField, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , 90)));

        // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
        blueTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , -90)));
        redTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));
//
        blueTowerGoalTarget.setName("Blue Tower Goal Target");
        redTowerGoalTarget.setName("Red Tower Goal Target");
        redAllianceTarget.setName("Red Alliance Target");
        blueAllianceTarget.setName("Blue Alliance Target");
        frontWallTarget.setName("Front Wall Target");

        //
        // Create a transformation matrix describing where the phone is on the robot.
        //
        // NOTE !!!!  It's very important that you turn OFF your phone's Auto-Screen-Rotation option.
        // Lock it into Portrait for these numbers to work.
        //
        // Info:  The coordinate frame for the robot looks the same as the field.
        // The robot's "forward" direction is facing out along X axis, with the LEFT side facing out along the Y axis.
        // Z is UP on the robot.  This equates to a bearing angle of Zero degrees.
        //
        // The phone starts out lying flat, with the screen facing Up and with the physical top of the phone
        // pointing to the LEFT side of the Robot.
        // The two examples below assume that the camera is facing forward out the front of the robot.

        // We need to rotate the camera around it's long axis to bring the correct camera forward.
        if (CAMERA_CHOICE == BACK) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90 ;
        }

        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT  = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot-center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT     = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        allTrackables.clear();
        allTrackables.addAll(targetsUltimateGoal);
        targetsUltimateGoal.activate();
    }

    // This detects the number of rings in front of the robot (for use in auton). Vuforia and TFOD must be initialized first.
    public boolean[] detectRings() {

        ArrayList<ringObject> rings = ringObject.detectRings(tfod);

        boolean is1ring = false;
        boolean is4rings = false;

        for (ringObject ring : rings) {
            is1ring = (ring.label.equals("Single"));
            is4rings = (ring.label.equals("Quad"));
        }

        return new boolean[]{is1ring, is4rings};
    }
    public Position detectTargets() {
        // check all the trackable targets to see which one (if any) is visible.
        String name = "";
        Position P;
        boolean targetVisible = false;
        for (VuforiaTrackable trackable : allTrackables) {
            if (((VuforiaTrackableDefaultListener)trackable.getListener()).isVisible()) {
                name = trackable.getName();
                targetVisible = true;

                // getUpdatedRobotLocation() will return null if no new information is available since
                // the last time that call was made, or if the trackable is not currently visible.
                OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener)trackable.getListener()).getUpdatedRobotLocation();
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform;
                }
                break;
            }
        }

        // Provide feedback as to where the robot is located (if we know).
        if (targetVisible) {
            // express position (translation) of robot in inches.
            VectorF translation = lastLocation.getTranslation();
            // express the rotation of the robot in degrees.
            Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
            // Send to variable
            P = new Position(name, translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch,
                    rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle);
        }
        else {
            P = null;
        }
        return P;
    }

    /** MATH/CALCULATION METHODS */
    // This method calculates the inches each mecanum wheel should turn to make the robot drive a certain number of x/y inches overall.
    // Positive x is to the right; Positive y is forward.
    public double[] calculateInches(double xInches, double yInches) {
        double r = Math.hypot(xInches, yInches);
        double robotAngle = Math.atan2(yInches, xInches) - Math.PI / 4;
        return new double[]{r * Math.cos(robotAngle), r * Math.sin(robotAngle), r * Math.sin(robotAngle), r * Math.cos(robotAngle)}; //fl,fr,bl,br
    }


}

