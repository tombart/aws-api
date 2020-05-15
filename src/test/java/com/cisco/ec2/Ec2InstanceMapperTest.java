package com.cisco.ec2;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.cisco.ec2.model.Ec2Instance;
import com.cisco.ec2.util.Ec2InstanceAssertions;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.cisco.ec2.Ec2InstanceMapper.NAME_TAG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

class Ec2InstanceMapperTest {

    @Test
    void shouldMapWithoutSorting() {
        Instance instance = new Instance().withPlacement(new Placement("eu-west-1"))
                .withInstanceId("instanceId")
                .withPrivateIpAddress("1.1.1.1.1")
                .withPublicIpAddress("2.2.2.2.2")
                .withInstanceType("ami1")
                .withTags(new Tag("Name", "myInstance"))
                .withState(new InstanceState().withName(InstanceStateName.Pending));

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(newArrayList(instance)));

        assertThat(result).hasSize(1);
        Ec2InstanceAssertions.assertEc2InstanceAttributes(result.get(0), instance);
    }

    @Test
    void shouldSortByName() {
        List<Instance> instances = generateInstancesWithNames("kkk", "vvv", "eee", "aaa");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "name");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getName()).isEqualTo("aaa");
        assertThat(result.get(1).getName()).isEqualTo("eee");
        assertThat(result.get(2).getName()).isEqualTo("kkk");
        assertThat(result.get(3).getName()).isEqualTo("vvv");
    }

    @Test
    void shouldSortByInstanceId() {
        List<Instance> instances = generateInstancesWithIds("a1", "5b", "123", "8-uio");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "instanceId");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getInstanceId()).isEqualTo("123");
        assertThat(result.get(1).getInstanceId()).isEqualTo("5b");
        assertThat(result.get(2).getInstanceId()).isEqualTo("8-uio");
        assertThat(result.get(3).getInstanceId()).isEqualTo("a1");
    }

    @Test
    void shouldSortByZones() {
        List<Instance> instances = generateInstancesWithZones("a1", "5b", "123", "8-uio");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "availabilityZone");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getAvailabilityZone()).isEqualTo("123");
        assertThat(result.get(1).getAvailabilityZone()).isEqualTo("5b");
        assertThat(result.get(2).getAvailabilityZone()).isEqualTo("8-uio");
        assertThat(result.get(3).getAvailabilityZone()).isEqualTo("a1");
    }

    @Test
    void shouldSortByTypes() {
        List<Instance> instances = generateInstancesWithTypes("a1", "5b", "123", "8-uio");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "type");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getType()).isEqualTo("123");
        assertThat(result.get(1).getType()).isEqualTo("5b");
        assertThat(result.get(2).getType()).isEqualTo("8-uio");
        assertThat(result.get(3).getType()).isEqualTo("a1");
    }

    @Test
    void shouldSortByState() {
        List<Instance> instances = generateInstancesWithStates("a1", "5b", "123", "8-uio");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "state");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getState()).isEqualTo("123");
        assertThat(result.get(1).getState()).isEqualTo("5b");
        assertThat(result.get(2).getState()).isEqualTo("8-uio");
        assertThat(result.get(3).getState()).isEqualTo("a1");
    }

    @Test
    void shouldSortByPublicIp() {
        List<Instance> instances = generateInstancesWithPublicIps("1.1.1.1", "2.3.4.5", "4.8.9.0.1", "2.2.2.2.2");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "publicIp");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getPublicIp()).isEqualTo("1.1.1.1");
        assertThat(result.get(1).getPublicIp()).isEqualTo("2.2.2.2.2");
        assertThat(result.get(2).getPublicIp()).isEqualTo("2.3.4.5");
        assertThat(result.get(3).getPublicIp()).isEqualTo("4.8.9.0.1");
    }

    @Test
    void shouldSortByPrivateIp() {
        List<Instance> instances = generateInstancesWithPrivateIps("1.1.1.1", "2.3.4.5", "4.8.9.0.1", "2.2.2.2.2");

        List<Ec2Instance> result = Ec2InstanceMapper.toEc2Instances(generateReservations(instances), "privateIp");

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getPrivateIp()).isEqualTo("1.1.1.1");
        assertThat(result.get(1).getPrivateIp()).isEqualTo("2.2.2.2.2");
        assertThat(result.get(2).getPrivateIp()).isEqualTo("2.3.4.5");
        assertThat(result.get(3).getPrivateIp()).isEqualTo("4.8.9.0.1");
    }

    private List<Reservation> generateReservations(List<Instance> instances) {
        List<Reservation> result = new ArrayList<>();
        result.add(new Reservation().withInstances(instances));
        return result;
    }

    private List<Instance> generateInstancesWithNames(String... names) {
        return Arrays.stream(names)
                .map(name -> generateInstance(name, null, null, null, null, null, null))
                .collect(Collectors.toList());
    }

    private List<Instance> generateInstancesWithIds(String... ids) {
        return Arrays.stream(ids)
                .map(id -> generateInstance(null, id, null, null, null, null, null))
                .collect(Collectors.toList());
    }

    private List<Instance> generateInstancesWithZones(String... zones) {
        return Arrays.stream(zones)
                .map(zone -> generateInstance(null, null, zone, null, null, null, null))
                .collect(Collectors.toList());
    }

    private List<Instance> generateInstancesWithTypes(String... types) {
        return Arrays.stream(types)
                .map(type -> generateInstance(null, null, null, type, null, null, null))
                .collect(Collectors.toList());
    }

    private List<Instance> generateInstancesWithStates(String... states) {
        return Arrays.stream(states)
                .map(state -> generateInstance(null, null, null, null, state, null, null))
                .collect(Collectors.toList());
    }

    private List<Instance> generateInstancesWithPrivateIps(String... ips) {
        return Arrays.stream(ips)
                .map(ip -> generateInstance(null, null, null, null, null, ip, null))
                .collect(Collectors.toList());
    }

    private List<Instance> generateInstancesWithPublicIps(String... ips) {
        return Arrays.stream(ips)
                .map(ip -> generateInstance(null, null, null, null, null, null, ip))
                .collect(Collectors.toList());
    }

    private Instance generateInstance(String name, String id, String avZone, String type, String state, String privIp, String pubIp) {
        return new Instance()
                .withPlacement(new Placement((avZone == null) ? RandomStringUtils.randomAlphabetic(5) : avZone))
                .withInstanceId((id == null) ? RandomStringUtils.randomAlphabetic(5) : id)
                .withPrivateIpAddress((privIp == null) ? RandomStringUtils.randomAlphabetic(5) : privIp)
                .withPublicIpAddress((pubIp == null) ? RandomStringUtils.randomAlphabetic(5) : pubIp)
                .withInstanceType((type == null) ? RandomStringUtils.randomAlphabetic(5) : type)
                .withTags(new Tag(NAME_TAG, (name == null) ? RandomStringUtils.randomAlphabetic(5) : name))
                .withState(new InstanceState().withName((state == null) ? RandomStringUtils.randomAlphabetic(5) : state));
    }
}