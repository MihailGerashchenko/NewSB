package com.example.newsb.service;

import com.example.newsb.entity.Customer;
import com.example.newsb.entity.Degree;
import com.example.newsb.entity.Test;
import com.example.newsb.repository.TestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService {
    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @Transactional
    public Test addTest(String subject, String question1, String question2,
                        String question3, String time, Degree degree, Customer customer) {

        Test test = new Test(subject, question1, question2, question3, time, degree);
        customer.addTest(test);
        testRepository.save(test);
        return test;
    }

    @Transactional()
    public Test addTestWithoutCustomer(String subject, String question1, String question2,
                                       String question3, String time, Degree degree) {
        Test test = new Test(subject, question1, question2, question3, time, degree);
        testRepository.save(test);
        return test;
    }

    @Transactional
    public Page<Test> findAll(Pageable pageable){
        return testRepository.findAllByOrderBySubject(pageable);
    }

    @Transactional
    public Page<Test> findBySubject(String subject, Pageable pageable){
        return testRepository.findBySubjectOrderBySubjectDesc(subject, pageable);
    }
}
