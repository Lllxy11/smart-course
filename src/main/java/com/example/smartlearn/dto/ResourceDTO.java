package com.example.smartlearn.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {
    private Long id;
    private String name;
    private String type;
    private String url;
    private String linkedAt;

//    public static ResourceDTO fromEntity(KnowledgePointResource entity) {
//        ResourceDTO dto = new ResourceDTO();
//        dto.setId(entity.getResourceId());
//        dto.setLinkedAt(entity.getLinkedAt().toString());
//        // 其他资源信息可以通过关联查询获取
//        return dto;
//    }

}