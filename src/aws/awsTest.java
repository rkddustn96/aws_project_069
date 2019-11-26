package aws;

import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;

import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;

import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;

import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.KeyPairInfo;

import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;

public class awsTest {

	static AmazonEC2 ec2;

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);

		}
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
	}

	public static void main(String[] args) throws Exception {
		init();
		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);

		int number = 0;
		String ins_id;

		while (true) {
			System.out.println(" ");
			System.out.println(" ");
			System.out.println("------------------------------------------------------------");
			System.out.println(" Amazon AWS Control Panel using SDK ");
			System.out.println(" ");
			System.out.println(" Cloud Computing, Computer Science Department ");
			System.out.println(" at Chungbuk National University ");
			System.out.println("------------------------------------------------------------");
			System.out.println(" 1. list instance 2. available zones ");
			System.out.println(" 3. start instance 4. available regions ");
			System.out.println(" 5. stop instance 6. create instance ");
			System.out.println(" 7. reboot instance 8. list images ");
			System.out.println(" 9. key pair list 99. quit ");
			System.out.println("------------------------------------------------------------");
			System.out.print("Enter an integer:");

			number = menu.nextInt();

			switch (number) {
			case 1:
				listInstances();
				break;
			case 2:
				DescribeZones();
				break;
			case 3:
				listInstances();
				System.out.print("\n Please select and wright your instance ID that you want to starting : ");
				ins_id = id_string.nextLine();
				startInstance(ins_id);
				break;
			case 4:
				DescribeRegions();
				break;
			case 5:
				listInstances();
				System.out.print("\n Please select and wright your instance ID that you want to stoping : ");
				ins_id = id_string.nextLine();
				stopInstance(ins_id);
				break;
			case 6:
				System.out.print("\n Please wright your img Name and ID that you want to using for create new instance \n"
						+ "[IAM name] : ");
				String Iam_name = id_string.nextLine();
				
				System.out.print("[IAM id] : ");
				String Iam_id = id_string.nextLine();
				
				System.out.print("\n Please wright your key-pair name \n"
						+ "[key-pair name] : ");
				String key_name = id_string.nextLine();
				
				CreateInstance(Iam_id, key_name);
				break;
			case 7:
				listInstances();
				System.out.print("\n Please select and wright your instance ID that you want to reboot : ");
				ins_id = id_string.nextLine();
				RebootInstance(ins_id);
				break;
			case 8:

				break;
			case 9:
				DescribeKeyPairs();
				break;
			case 99:

				break;
			}
		}

	}

	// #1 show instance list
	public static void listInstances() {
		System.out.println("Listing instances....");
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					System.out.printf(
							"[id] %s, " + "[AMI] %s, " + "[type] %s, " + "[state] %10s, " + "[monitoring state] %s",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getMonitoring().getState());
				}
				System.out.println();
			}
			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}

		}

	}

	// #3 start instance using instance_id
	public static void startInstance(String instance_id) {

		DryRunSupportedRequest<StartInstancesRequest> dry_request = () -> {
			StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		DryRunResult dry_response = ec2.dryRun(dry_request);

		if (!dry_response.isSuccessful()) {
			System.out.printf("Failed dry run to start instance %s", instance_id);

			throw dry_response.getDryRunResponse();
		}

		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}

	// #5stop instance using instance id
	public static void stopInstance(String instance_id) {

		DryRunSupportedRequest<StopInstancesRequest> dry_request = () -> {
			StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		DryRunResult dry_response = ec2.dryRun(dry_request);

		if (!dry_response.isSuccessful()) {
			System.out.printf("Failed dry run to stop instance %s", instance_id);
			throw dry_response.getDryRunResponse();
		}

		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);

		ec2.stopInstances(request);

		System.out.printf("Successfully stop instance %s", instance_id);
	}

	// #7 reboot instance id using instance id
	public static void RebootInstance(String input_id) {

		if (input_id.length() == 0) {
			System.out.println("not found instance");
			System.exit(1);
		}

		String instance_id = input_id;

		RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instance_id);

		RebootInstancesResult response = ec2.rebootInstances(request);

		System.out.printf("Successfully rebooted instance %s", instance_id);
	}
	// #2 show available zone list

	public static void DescribeZones() {

		DescribeAvailabilityZonesResult zones_response = ec2.describeAvailabilityZones();

		for (AvailabilityZone zone : zones_response.getAvailabilityZones()) {
			System.out.printf(" [availability zone] %s " + " [status] %s " + " [region] %s \n", zone.getZoneName(),
					zone.getState(), zone.getRegionName());
		}

	}

	// #4 show available region list
	public static void DescribeRegions() {

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for (Region region : regions_response.getRegions()) {
			System.out.printf(" [region] %s " + " [endpoint] %s \n", region.getRegionName(), region.getEndpoint());
		}

	}

	// #9 show key pair list
	public static void DescribeKeyPairs() {

		DescribeKeyPairsResult response = ec2.describeKeyPairs();

		for (KeyPairInfo key_pair : response.getKeyPairs()) {
			System.out.printf("[name] %s " + " [fingerprint] %s \n", key_pair.getKeyName(),
					key_pair.getKeyFingerprint());
		}

	}

	// #6 Create Instance
	public static void CreateInstance(String ami_idd, String key_n) {

		String ami_id = ami_idd;

		RunInstancesRequest run_request = new RunInstancesRequest().withImageId(ami_id)
				.withInstanceType(InstanceType.T2Micro).withMaxCount(1).withMinCount(1)
				.withKeyName(key_n);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf("Successfully started EC2 instance %s based on AMI %s with %s key-pair", reservation_id, ami_id, key_n);
	}

}