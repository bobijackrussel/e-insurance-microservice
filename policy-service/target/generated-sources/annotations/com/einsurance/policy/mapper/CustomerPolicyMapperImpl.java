package com.einsurance.policy.mapper;

import com.einsurance.common.dto.CustomerPolicyDto;
import com.einsurance.policy.entity.CustomerPolicy;
import com.einsurance.policy.entity.PolicyTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-02T16:38:23+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class CustomerPolicyMapperImpl implements CustomerPolicyMapper {

    @Override
    public CustomerPolicyDto toDto(CustomerPolicy policy) {
        if ( policy == null ) {
            return null;
        }

        CustomerPolicyDto.CustomerPolicyDtoBuilder customerPolicyDto = CustomerPolicyDto.builder();

        customerPolicyDto.policyTemplateId( policyPolicyTemplateId( policy ) );
        customerPolicyDto.policyTemplateName( policyPolicyTemplateName( policy ) );
        customerPolicyDto.id( policy.getId() );
        customerPolicyDto.policyNumber( policy.getPolicyNumber() );
        customerPolicyDto.userId( policy.getUserId() );
        customerPolicyDto.purchaseDate( policy.getPurchaseDate() );
        customerPolicyDto.startDate( policy.getStartDate() );
        customerPolicyDto.expiryDate( policy.getExpiryDate() );
        if ( policy.getStatus() != null ) {
            customerPolicyDto.status( policy.getStatus().name() );
        }
        customerPolicyDto.paymentTransactionId( policy.getPaymentTransactionId() );
        customerPolicyDto.totalAmount( policy.getTotalAmount() );
        customerPolicyDto.createdAt( policy.getCreatedAt() );
        customerPolicyDto.updatedAt( policy.getUpdatedAt() );

        customerPolicyDto.policyType( mapPolicyType(policy.getPolicyTemplate()) );
        customerPolicyDto.remainingDays( mapRemainingDays(policy) );

        return customerPolicyDto.build();
    }

    @Override
    public List<CustomerPolicyDto> toDtoList(List<CustomerPolicy> policies) {
        if ( policies == null ) {
            return null;
        }

        List<CustomerPolicyDto> list = new ArrayList<CustomerPolicyDto>( policies.size() );
        for ( CustomerPolicy customerPolicy : policies ) {
            list.add( toDto( customerPolicy ) );
        }

        return list;
    }

    private UUID policyPolicyTemplateId(CustomerPolicy customerPolicy) {
        if ( customerPolicy == null ) {
            return null;
        }
        PolicyTemplate policyTemplate = customerPolicy.getPolicyTemplate();
        if ( policyTemplate == null ) {
            return null;
        }
        UUID id = policyTemplate.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String policyPolicyTemplateName(CustomerPolicy customerPolicy) {
        if ( customerPolicy == null ) {
            return null;
        }
        PolicyTemplate policyTemplate = customerPolicy.getPolicyTemplate();
        if ( policyTemplate == null ) {
            return null;
        }
        String name = policyTemplate.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
