package com.cisco.ec2;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.cisco.ec2.model.Ec2Instance;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class Ec2InstanceMapper {

    private static final Comparator<Ec2Instance> DEFAULT_COMPARATOR_BY_INSTANCE_ID = Comparator.comparing(Ec2Instance::getInstanceId);
    private static final Map<String, Comparator<Ec2Instance>> INSTANCE_COMPARATORS = new HashMap<String, Comparator<Ec2Instance>>() {{
        put("instanceId", DEFAULT_COMPARATOR_BY_INSTANCE_ID);
        put("name", Comparator.comparing(Ec2Instance::getName));
        put("type", Comparator.comparing(Ec2Instance::getType));
        put("state", Comparator.comparing(Ec2Instance::getState));
        put("availabilityZone", Comparator.comparing(Ec2Instance::getAvailabilityZone));
        put("privateIp", Comparator.comparing(Ec2Instance::getPrivateIp));
        put("publicIp", Comparator.comparing(Ec2Instance::getPublicIp));
    }};
    public static final String NAME_TAG = "Name";

    static List<Ec2Instance> toEc2Instances(List<Reservation> reservations) {
        return toEc2Instances(reservations, null);
    }

    static List<Ec2Instance> toEc2Instances(List<Reservation> reservations, String sortBy) {
        return reservations.stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .map(Ec2InstanceMapper::toEc2Instance)
                .sorted(INSTANCE_COMPARATORS.getOrDefault(sortBy, DEFAULT_COMPARATOR_BY_INSTANCE_ID))
                .collect(toList());
    }

    private static Ec2Instance toEc2Instance(Instance instance) {
        return Ec2Instance.builder()
                .instanceId(instance.getInstanceId())
                .type(instance.getInstanceType())
                .privateIp(instance.getPrivateIpAddress())
                .publicIp(instance.getPublicIpAddress())
                .name(getName(instance).orElse(null))
                .state(instance.getState().getName())
                .availabilityZone(getAvailabilityZone(instance).orElse(null))
                .build();
    }

    private static Optional<String> getName(Instance instance) {
        return instance.getTags().stream()
                .filter(tag -> tag.getKey().equalsIgnoreCase(NAME_TAG))
                .map(Tag::getValue).findFirst();
    }

    private static Optional<String> getAvailabilityZone(Instance instance) {
        return Optional.ofNullable(instance.getPlacement())
                .map(Placement::getAvailabilityZone);
    }
}
