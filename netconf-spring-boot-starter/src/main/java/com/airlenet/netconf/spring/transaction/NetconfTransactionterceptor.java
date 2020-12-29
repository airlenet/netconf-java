package com.airlenet.netconf.spring.transaction;

import com.airlenet.netconf.datasource.NetconfDataSource;
import com.airlenet.netconf.datasource.NetconfPooledConnection;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class NetconfTransactionterceptor implements MethodInterceptor {
    protected NetconfDataSource netconfDataSource;
    private ExpressionParser parser = new SpelExpressionParser();

    private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        NetconfTransactional annotation = methodInvocation.getMethod().getAnnotation(NetconfTransactional.class);
        String[] params = discoverer.getParameterNames(methodInvocation.getMethod());
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], methodInvocation.getArguments()[len]);
        }
        String url = parser.parseExpression(annotation.url()).getValue(context, String.class);
        String username = parser.parseExpression(annotation.username()).getValue(context, String.class);
        String password = parser.parseExpression(annotation.password()).getValue(context, String.class);

        NetconfPooledConnection connection;
        Object proceed = null;
        //todo 增加transactionManage，待需要时 开启事务，即editconfig操作
        try (NetconfPooledConnection pooledConnection = (netconfDataSource).getConnection(url, username, password);) {
            try {
                pooledConnection.setAutoCommit(false);
                pooledConnection.startTransaction();
                proceed = methodInvocation.proceed();
                pooledConnection.commitTransaction();
            } catch (Exception e) {
                throw pooledConnection.getCauseException(e);
            } finally {
                pooledConnection.setAutoCommit(true);
                pooledConnection.unlock();
            }
            return proceed;
        } catch (Exception e) {
            throw e;
        }
    }

    public void setNetconfDataSource(NetconfDataSource multiNetconfDataSource) {
        this.netconfDataSource = multiNetconfDataSource;
    }
}
