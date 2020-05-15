package com.cisco.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Ec2ClientProviderTest {

    @Test
    void shouldReturnClient() {
        Ec2ClientProvider ec2ClientProvider = new Ec2ClientProvider();

        AmazonEC2 amazonEC2 = ec2ClientProvider.getClient("abc");

        assertThat(amazonEC2).isNotNull();
    }
}