package com.einsurance.claims.mapper;

import com.einsurance.claims.entity.Claim;
import com.einsurance.common.dto.ClaimDto;
import com.einsurance.common.dto.ClaimSubmissionRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-02T13:52:46+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Oracle Corporation)"
)
@Component
public class ClaimMapperImpl implements ClaimMapper {

    @Override
    public ClaimDto toDto(Claim claim) {
        if ( claim == null ) {
            return null;
        }

        ClaimDto.ClaimDtoBuilder claimDto = ClaimDto.builder();

        if ( claim.getStatus() != null ) {
            claimDto.status( claim.getStatus().name() );
        }
        claimDto.id( claim.getId() );
        claimDto.claimNumber( claim.getClaimNumber() );
        claimDto.userId( claim.getUserId() );
        claimDto.customerPolicyId( claim.getCustomerPolicyId() );
        claimDto.amount( claim.getAmount() );
        claimDto.description( claim.getDescription() );
        claimDto.incidentDate( claim.getIncidentDate() );
        claimDto.submittedDate( claim.getSubmittedDate() );
        claimDto.reviewedDate( claim.getReviewedDate() );
        claimDto.reviewedBy( claim.getReviewedBy() );
        claimDto.adminNotes( claim.getAdminNotes() );
        claimDto.createdAt( claim.getCreatedAt() );
        claimDto.updatedAt( claim.getUpdatedAt() );

        return claimDto.build();
    }

    @Override
    public List<ClaimDto> toDtoList(List<Claim> claims) {
        if ( claims == null ) {
            return null;
        }

        List<ClaimDto> list = new ArrayList<ClaimDto>( claims.size() );
        for ( Claim claim : claims ) {
            list.add( toDto( claim ) );
        }

        return list;
    }

    @Override
    public Claim toEntity(ClaimSubmissionRequest request) {
        if ( request == null ) {
            return null;
        }

        Claim.ClaimBuilder claim = Claim.builder();

        claim.customerPolicyId( request.getCustomerPolicyId() );
        claim.amount( request.getAmount() );
        claim.description( request.getDescription() );
        claim.incidentDate( request.getIncidentDate() );

        return claim.build();
    }
}
