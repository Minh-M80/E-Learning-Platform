
package com.example.E_Learning_Platform.service.rag;

import com.example.E_Learning_Platform.enums.Role;
import java.util.Set;

public record RagScope(
        String userId,
        Set<Role> roles,
        Set<String> enrolledCourseIds,
        Set<String> instructorCourseIds
) {
    public boolean isAdmin() { return roles.contains(Role.ADMIN); }
    public boolean isInstructor() { return roles.contains(Role.INSTRUCTOR); }
}
