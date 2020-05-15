import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Tag;
import com.cisco.Application;
import com.cisco.ec2.Ec2ClientProvider;
import com.cisco.ec2.model.Ec2Instance;
import com.cisco.ec2.model.GetEc2Response;
import com.cisco.ec2.util.Ec2InstanceAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static com.cisco.ec2.util.Ec2ModelGenerator.generateInstancesResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
class ComponentTest {
    @LocalServerPort
    private int randomServerPort;
    @Autowired
    private TestRestTemplate restTemplate;
    @MockBean
    private Ec2ClientProvider ec2ClientProvider;
    @Mock
    private AmazonEC2 ec2Client;

    @Test
    void shouldReturnInstancesForRegion() {
        String region = "eu-west-1";
        Instance instance = new Instance().withPlacement(new Placement(region))
                .withInstanceId("instanceId")
                .withPrivateIpAddress("1.1.1.1.1")
                .withPublicIpAddress("2.2.2.2.2")
                .withInstanceType("ami1")
                .withTags(new Tag("Name", "myInstance"))
                .withState(new InstanceState().withName(InstanceStateName.Pending));
        when(ec2Client.describeInstances(new DescribeInstancesRequest())).thenReturn(generateInstancesResult(instance));
        when(ec2ClientProvider.getClient(region)).thenReturn(ec2Client);

        ResponseEntity<GetEc2Response> response = restTemplate.getForEntity(String.format("http://localhost:%s/aws-api/v1/regions/%s/ec2",
                randomServerPort, region), GetEc2Response.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Ec2Instance> responseInstances = Objects.requireNonNull(response.getBody()).getInstances();
        assertThat(responseInstances).hasSize(1);
        Ec2InstanceAssertions.assertEc2InstanceAttributes(responseInstances.get(0), instance);
    }
}
