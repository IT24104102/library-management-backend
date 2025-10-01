package com.kuruneru.latereminders.service;

import com.kuruneru.latereminders.dto.LoanDto;
import com.kuruneru.latereminders.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "library-reminders.scheduler.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.profiles.active=test",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration"
})
class ReminderServiceTest {

    @Test
    void testLoanDueTomorrow() {
        LoanDto loan = new LoanDto();
        loan.setId(1L);
        loan.setUserId(1L);
        loan.setBookIsbn("978-0123456789");
        loan.setDueDate(LocalDateTime.now().plusDays(1));
        loan.setStatus("ACTIVE");
        
        assertTrue(loan.isDueTomorrow());
        assertFalse(loan.isOverdue());
    }
    
    @Test
    void testOverdueLoan() {
        LoanDto loan = new LoanDto();
        loan.setId(1L);
        loan.setUserId(1L);
        loan.setBookIsbn("978-0123456789");
        loan.setDueDate(LocalDateTime.now().minusDays(1));
        loan.setStatus("ACTIVE");
        
        assertTrue(loan.isOverdue());
        assertFalse(loan.isDueTomorrow());
    }
    
    @Test
    void testUserDto() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRole("STUDENT");
        
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertTrue(user.getEmail().contains("@"));
    }
}