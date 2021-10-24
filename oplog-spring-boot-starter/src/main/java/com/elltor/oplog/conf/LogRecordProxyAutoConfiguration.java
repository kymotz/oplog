package com.elltor.oplog.conf;

import com.elltor.oplog.annotation.EnableLogRecord;
import com.elltor.oplog.annotation.LogRecord;
import com.elltor.oplog.core.LogRecordExpressionEvaluator;
import com.elltor.oplog.factory.LogRecordOperationFactory;
import com.elltor.oplog.core.LogRecordValueParser;
import com.elltor.oplog.factory.ParseFunctionFactory;
import com.elltor.oplog.service.ILogRecordService;
import com.elltor.oplog.service.IOperatorGetService;
import com.elltor.oplog.service.IParseFunction;
import com.elltor.oplog.service.impl.DefaultLogRecordServiceImpl;
import com.elltor.oplog.service.impl.DefaultOperatorGetServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

@Configuration
@ConditionalOnClass(LogRecord.class)
@ComponentScan(basePackages = {"com.elltor.oplog.*"})
public class LogRecordProxyAutoConfiguration implements ImportAware {

    private AnnotationAttributes enableLogRecord;

    private Logger log = LoggerFactory.getLogger(LogRecordProxyAutoConfiguration.class);

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationFactory logRecordOperationFactory() {
        return new LogRecordOperationFactory();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ParseFunctionFactory parseFunctionFactory(@Autowired List<IParseFunction> parseFunctions) {
        return new ParseFunctionFactory(parseFunctions);
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
            log.info("@EnableLogRecord is not present on importing class");
        }
    }

}