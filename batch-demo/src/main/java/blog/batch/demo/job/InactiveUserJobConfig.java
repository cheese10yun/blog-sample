package blog.batch.demo.job;

import blog.batch.demo.domain.User;
import blog.batch.demo.domain.enums.UserStatus;
import blog.batch.demo.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Configuration
public class InactiveUserJobConfig {
    private UserRepository userRepository;

    @Bean
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep) {
        return jobBuilderFactory.get("inactiveUserJob")
                .preventRestart()
                .start(inactiveJobStep)
                .build();
    }

    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("inactiveUserStep")
                .<User, User> chunk(10)
                .reader(inactiveUserReader())
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<User> inactiveUserReader() {
        List<User> oldUsers =
                userRepository.findByUpdatedDateBeforeAndStatusEquals(
                        LocalDateTime.now().minusYears(1),
                        UserStatus.ACTIVE);

        Map<String ,Object> map = new HashMap<>();

        return new ListItemReader<>(oldUsers);
    }


    @Bean
    @StepScope
    public JpaPagingItemReader<User> inactiveUserReader2() {


        final JpaPagingItemReader reader = new JpaPagingItemReader(){

            @Override
            public int getPage(){
                return 0;
            }
        };





    }

    public ItemProcessor<User, User> inactiveUserProcessor() {
        return User::setInactive;
    }

    public ItemWriter<User> inactiveUserWriter() {
        return userRepository::saveAll;
    }

}
