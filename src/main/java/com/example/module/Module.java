package com.example.module;

import com.example.module_group.ModuleGroup;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(length = 255)
    private String icon;

    @ManyToOne
    @JoinColumn(name = "module_group_id", nullable = false)
    private ModuleGroup moduleGroup;

    @Override
    public String toString() {
        return name;
    }
}
