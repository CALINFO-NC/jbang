package scripts.jbang.service;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FieldProperty {

    private String name;
    private String type;
    private List<String> typeParam = new ArrayList<>();
}
