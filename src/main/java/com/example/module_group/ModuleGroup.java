package com.example.module_group;

import com.example.module.Module;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(
        name = "module_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_name"})
)
public class ModuleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Group name is mandatory")
    @Column(name = "group_name", nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "moduleGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Module> modules;

    @Override
    public String toString() {
        return name;
    }
}
