package eu.ibagroup.vfjobstorageservice.mappers;

import eu.ibagroup.vfjobstorageservice.dto.jobs.JobDto;
import eu.ibagroup.vfjobstorageservice.model.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JobDtoMapper {

    JobDtoMapper INSTANCE = Mappers.getMapper(JobDtoMapper.class);
    @Mapping(target = "id",
            expression = "java(jobDto.getId() != null ? jobDto.getId() : java.util.UUID.randomUUID().toString())")
    Job dtoToEntity(JobDto jobDto);
    JobDto entityToDto(Job job);
}
