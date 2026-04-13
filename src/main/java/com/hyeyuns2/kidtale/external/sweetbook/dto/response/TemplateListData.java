package com.hyeyuns2.kidtale.external.sweetbook.dto.response;

import java.util.List;

public record TemplateListData(
        List<Template> templates,
        Pagination pagination
) {}
