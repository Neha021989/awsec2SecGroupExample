package com.amazonaws.samples;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.util.CollectionUtils;

public class InitializeLinuxInstance {

	public static void main(String[] args) {

		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();

		// create a security group if not already exist

		List<SecurityGroup> javaSecurityGroup = ec2.describeSecurityGroups().getSecurityGroups().stream()
				.filter(x -> x.getGroupName().equals("JavaSecurityGroup")).collect(Collectors.toList());

		if (CollectionUtils.isNullOrEmpty(javaSecurityGroup)) {
			CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
			csgr.withGroupName("JavaSecurityGroup").withDescription("My security group");

			CreateSecurityGroupResult createSecurityGroupResult = ec2.createSecurityGroup(csgr);

			IpPermission ipPermission = new IpPermission();

			IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");

			ipPermission.withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange })).withIpProtocol("tcp").withFromPort(22)
					.withToPort(22);

			AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();
			authorizeSecurityGroupIngressRequest.withGroupName("JavaSecurityGroup").withIpPermissions(ipPermission);
			ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
		}

		// Create a key pair

		List<KeyPairInfo> keyPairs = ec2.describeKeyPairs().getKeyPairs().stream()
				.filter(x -> x.getKeyName().equals("ec2Key")).collect(Collectors.toList());

		if (CollectionUtils.isNullOrEmpty(keyPairs)) {
			CreateKeyPairRequest keyPairRequest = new CreateKeyPairRequest();
			keyPairRequest.withKeyName("ec2Key");

			CreateKeyPairResult createKeyPairResult = ec2.createKeyPair(keyPairRequest);

			KeyPair keyPair = createKeyPairResult.getKeyPair();
			String privateKey = keyPair.getKeyMaterial();

			System.out.println("key is " + privateKey);
		}

		// create an ec2 instance

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		runInstancesRequest.withImageId("ami-0bcf5425cdc1d8a85").withInstanceType(InstanceType.T2Micro).withMinCount(1)
				.withMaxCount(1).withKeyName("ec2Key").withSecurityGroups("JavaSecurityGroup");

		RunInstancesResult runInstanceResult = ec2.runInstances(runInstancesRequest);

		String instanceId = runInstanceResult.getReservation().getInstances().get(0).getInstanceId();

		try {
			Thread.sleep(200000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Stopping the ec2 instance

		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instanceId);

		ec2.stopInstances(stopInstancesRequest);

	}
}
