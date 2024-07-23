package eu.ibagroup.vfjobstorageservice.mappers;

import eu.ibagroup.vfjobstorageservice.dto.jobs.JobOverviewDto;
import eu.ibagroup.vfjobstorageservice.model.Job;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JobOverviewMapper {

    JobOverviewMapper INSTANCE = Mappers.getMapper(JobOverviewMapper.class);
    Job dtoToEntity(JobOverviewDto jobOverviewDto);
    JobOverviewDto entityToDto(Job job);
}
