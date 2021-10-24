package com.elltor.oplog.conf;

import com.elltor.oplog.annotation.EnableLogRecord;
import com.elltor.oplog.core.LogRecordExpressionEvaluator;
import com.elltor.oplog.core.LogRecordOperationSource;
import com.elltor.oplog.core.LogRecordValueParser;
import com.elltor.oplog.factory.ParseFunctionFactory;
import com.elltor.oplog.service.ILogRecordService;
import com.elltor.oplog.service.IOperatorGetService;
import com.elltor.oplog.service.IParseFunction;
import com.elltor.oplog.service.impl.CalcParseFunction;
import com.elltor.oplog.service.impl.DefaultLogRecordServiceImpl;
import com.elltor.oplog.service.impl.DefaultOperatorGetServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

@Configuration
@ComponentScan(basePackages = {"com.elltor.oplog.*"})
@EnableConfigurationProperties(LogRecordConfigProperties.class)
public class LogRecordProxyAutoConfiguration implements ImportAware {

    private AnnotationAttributes enableLogRecord;

    private LogRecordConfigProperties logRecordConfigProperties;

    // 构造器注入
    public LogRecordProxyAutoConfiguration(LogRecordConfigProperties logRecordConfigProperties) {
        this.logRecordConfigProperties = logRecordConfigProperties;
        System.out.println("构造器注入 starter");
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationSource logRecordOperationSource() {
        return new LogRecordOperationSource();
    }

    @Bean
    public ParseFunctionFactory parseFunctionFactory(@Autowired List<IParseFunction> parseFunctions) {
        return new ParseFunctionFactory(parseFunctions);
    }

    @Bean
    @ConditionalOnMissingBean(IParseFunction.class)
    public CalcParseFunction parseFunction() {
        return new CalcParseFunction();
    }

    @Bean
    LogRecordExpressionEvaluator logRecordExpressionEvaluator() {
        return new LogRecordExpressionEvaluator();
    }

    @Bean
    public LogRecordValueParser logRecordValueParser(LogRecordExpressionEvaluator logRecordExpressionEvaluator) {
        return new LogRecordValueParser(null, logRecordExpressionEvaluator);
    }

    @Bean
    @ConditionalOnMissingBean(IOperatorGetService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IOperatorGetService operatorGetService() {
        return new DefaultOperatorGetServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ILogRecordService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public ILogRecordService recordService() {
        return new DefaultLogRecordServiceImpl();
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLogRecord = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (this.enableLogRecord == null) {
//            log.info("@EnableCaching is not present on importing class");
            System.err.println("@EnableCaching is not present on importing class");
        }
    }
}