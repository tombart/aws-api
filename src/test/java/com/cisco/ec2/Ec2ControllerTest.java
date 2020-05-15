package com.cisco.ec2;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Tag;
import com.cisco.ec2.util.Ec2ModelGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.stream.IntStream;

import static com.cisco.ec2.Ec2InstanceMapper.NAME_TAG;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(Ec2Controller.class)
class Ec2ControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private Ec2ClientProvider ec2ClientProvider;
    @Mock
    private AmazonEC2 ec2Client;

    @Test
    void shouldReturnValidResult() throws Exception {
        String region = "eu-west-1";
        Instance instance = new Instance().withPlacement(new Placement(region))
                .withInstanceId("instanceId")
                .withPrivateIpAddress("1.1.1.1.1")
                .withPublicIpAddress("2.2.2.2.2")
                .withInstanceType("ami1")
                .withTags(new Tag("Name", "myInstance"))
                .withState(new InstanceState().withName(InstanceStateName.Pending));
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenReturn(Ec2ModelGenerator.generateInstancesResult(instance));
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", region)))
                .andExpect(matchAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.instances[*]").value(hasSize(1)),
                        jsonPath("$.instances[0].name").value("myInstance"),
                        jsonPath("$.instances[0].instanceId").value("instanceId"),
                        jsonPath("$.instances[0].state").value("pending"),
                        jsonPath("$.instances[0].type").value("ami1"),
                        jsonPath("$.instances[0].availabilityZone").value("eu-west-1"),
                        jsonPath("$.instances[0].privateIp").value("1.1.1.1.1"),
                        jsonPath("$.instances[0].publicIp").value("2.2.2.2.2")));
    }

    @Test
    void shouldReturnAllInstances() throws Exception {
        String region = "eu-west-1";
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenReturn(Ec2ModelGenerator.generateInstancesResult(generateInstances(region, 5)));
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", region)))
                .andExpect(matchAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.instances[*]").value(hasSize(5))));
    }

    @Test
    void shouldPassParamsToDownstreamCall() throws Exception {
        String region = "eu-west-1";
        int size = 7;
        String sort = "instanceId";
        String nextToken = "abc";
        DescribeInstancesRequest request = new DescribeInstancesRequest().withNextToken(nextToken).withMaxResults(size);
        when(ec2Client.describeInstances(eq(request))).thenReturn(Ec2ModelGenerator.generateInstancesResult(generateInstances(region, 5)));
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2?size=%s&nextToken=%s&sort=%s", region, size, nextToken, sort)))
                .andExpect(matchAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.instances[*]").value(hasSize(5))));
        verify(ec2Client, times(1)).describeInstances(request);
    }

    @Test
    void shouldRetrieveAmazonClientForRegion() throws Exception {
        String region = "eu-west-1";
        when(ec2Client.describeInstances(eq(new DescribeInstancesRequest()))).thenReturn(Ec2ModelGenerator.generateInstancesResult(generateInstances(region, 5)));
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", region)))
                .andExpect(status().isOk());

        verify(ec2ClientProvider, times(1)).getClient(region);
    }

    @Test
    void shouldReturnNotFoundWhenRegionIsMissing() throws Exception {
        mockMvc.perform(get("/aws-api/v1/regions//ec2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotExceedMaxSizeOfRegionLength() throws Exception {
        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", RandomStringUtils.randomAlphanumeric(25))))
                .andExpect(matchAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorMessage").value("getEc2Instances.region: size must be between 0 and 20"),
                        jsonPath("$.status").value(400)));
    }

    @Test
    void shouldMeetMinSizeRequirement() throws Exception {
        mockMvc.perform(get("/aws-api/v1/regions/eu-west-1/ec2?size=1"))
                .andExpect(matchAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorMessage").value("getEc2Instances.size: must be greater than or equal to 5"),
                        jsonPath("$.status").value(400)));
    }

    @Test
    void shouldNotExceedMaxSizeOfResult() throws Exception {
        mockMvc.perform(get("/aws-api/v1/regions/eu-west-1/ec2?size=10000"))
                .andExpect(matchAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorMessage").value("getEc2Instances.size: must be less than or equal to 500"),
                        jsonPath("$.status").value(400)));
    }

    @Test
    void shouldNotExceedMaxLengthOfNextTokenParam() throws Exception {
        mockMvc.perform(get("/aws-api/v1/regions/eu-west-1/ec2?nextToken=" + RandomStringUtils.randomAlphanumeric(1025)))
                .andExpect(matchAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorMessage").value("getEc2Instances.nextToken: size must be between 0 and 1024"),
                        jsonPath("$.status").value(400)));
    }

    @Test
    void shouldNotExceedMaxSortParamLength() throws Exception {
        mockMvc.perform(get("/aws-api/v1/regions/eu-west-1/ec2?sort=" + RandomStringUtils.randomAlphanumeric(257)))
                .andExpect(matchAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.errorMessage").value("getEc2Instances.sort: size must be between 0 and 256"),
                        jsonPath("$.status").value(400)));
    }

    @Test
    void shouldSortResult() throws Exception {
        String region = "eu-west-1";
        Instance instanceA = new Instance().withPlacement(new Placement(region))
                .withInstanceId("instanceIdA")
                .withPrivateIpAddress("1.1.1.1.1")
                .withPublicIpAddress("2.2.2.2.2")
                .withInstanceType("ami1")
                .withTags(new Tag("Name", "AmyFirst"))
                .withState(new InstanceState().withName(InstanceStateName.Pending));
        Instance instanceB = new Instance().withPlacement(new Placement(region))
                .withInstanceId("instanceId")
                .withPrivateIpAddress("1.1.1.1.1")
                .withPublicIpAddress("2.2.2.2.2")
                .withInstanceType("ami1")
                .withTags(new Tag("Name", "BmySecond"))
                .withState(new InstanceState().withName(InstanceStateName.Pending));
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenReturn(Ec2ModelGenerator.generateInstancesResult(instanceB, instanceA));
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2?sort=name", region)))
                .andExpect(matchAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.instances[*]").value(hasSize(2)),
                        jsonPath("$.instances[0].name").value("AmyFirst"),
                        jsonPath("$.instances[1].name").value("BmySecond")));
    }

    @Test
    void shouldPropagateAwsErrorDetailsOnAmazonServiceException() throws Exception {
        String region = "eu-west-1";
        AmazonServiceException exception = buildException("error", 403, "123abc");
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenThrow(exception);

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", region)))
                .andExpect(matchAll(
                        status().isForbidden(),
                        jsonPath("$.errorMessage").value("Downstream failure"),
                        jsonPath("$.requestId").value("123abc"),
                        jsonPath("$.errorType").value("Client"),
                        jsonPath("$.status").value("403")));
    }

    @Test
    void shouldReturn400OnConstraintViolationException() throws Exception {
        String region = "eu-west-1";
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenThrow(new ConstraintViolationException("error", new HashSet<>()));

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", region)))
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.errorMessage").value("error"),
                        jsonPath("$.status").value("400")));
    }

    @Test
    void shouldReturn500OnRuntimeException() throws Exception {
        String region = "eu-west-1";
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get(String.format("/aws-api/v1/regions/%s/ec2", region)))
                .andExpect(matchAll(
                        status().isInternalServerError(),
                        jsonPath("$.errorMessage").value("Couldn't process request"),
                        jsonPath("$.status").value("500")));
    }

    private AmazonServiceException buildException(String message, int status, String id) {
        AmazonServiceException exception = new AmazonServiceException(message);
        exception.setErrorType(AmazonServiceException.ErrorType.Client);
        exception.setRequestId(id);
        exception.setStatusCode(status);
        return exception;
    }

    private Instance[] generateInstances(String region, int size) {
        Instance[] result = new Instance[size];
        IntStream.range(0, size)
                .forEach(i ->
                        result[i] = new Instance()
                                .withPlacement(new Placement(region))
                                .withInstanceId(RandomStringUtils.randomAlphabetic(5))
                                .withPrivateIpAddress(RandomStringUtils.randomAlphabetic(5))
                                .withPublicIpAddress(RandomStringUtils.randomAlphabetic(5))
                                .withInstanceType(RandomStringUtils.randomAlphabetic(5))
                                .withTags(new Tag(NAME_TAG, RandomStringUtils.randomAlphabetic(5)))
                                .withState(new InstanceState().withName(RandomStringUtils.randomAlphabetic(5))));
        return result;
    }
}