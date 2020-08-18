package com.airlenet.netconf.callhome;

import org.apache.sshd.common.util.security.SecurityUtils;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;

public class CallHomeAuthProviderImpl implements CallHomeAuthorizationProvider, AutoCloseable {

    @Override
    public CallHomeAuthorization provideAuth(SocketAddress remoteAddress, PublicKey serverKey) {


        String sessionName = "sessionName";
        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(
                ASN1Sequence.getInstance(serverKey.getEncoded()));

        subjectPublicKeyInfo.getPublicKeyData();


        try {
            CertificateFactory cf = SecurityUtils.getCertificateFactory("X.509");

            cf.generateCertificate(null);
            java.security.cert.X509Certificate x509Certificate = null;
//        x509Certificate.verify();
            x509Certificate.getSubjectX500Principal().getName();

            LdapName ldapName = new LdapName("");
            ldapName.getRdn(0).getType();//"CN";
        } catch (InvalidNameException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        CallHomeAuthorization.Builder authBuilder = CallHomeAuthorization.serverAccepted(sessionName, "admin");
        authBuilder.addPassword("admin");
        authBuilder.addPassword("123456");


        // 创建X509工厂类
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        //CertificateFactory cf = CertificateFactory.getInstance("X509");
//        // 创建证书对象
//        X509Certificate oCert = (X509Certificate) cf
//                .generateCertificate(inStream);
//
//        oCert.getSubjectDN().getName();
        return authBuilder.build();
    }

    @Override
    public void close() throws Exception {

    }


}
