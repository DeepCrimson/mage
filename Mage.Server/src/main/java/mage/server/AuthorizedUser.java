package mage.server;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.util.ByteSource;

import java.util.Date;

public class AuthorizedUser {

    protected String name;

    protected String password;

    protected String salt;

    protected String hashAlgorithm;

    protected int hashIterations;

    protected String email;

    protected boolean active; // the user can't sign in

    protected Date lockedUntil; // the user can't sign in until timestamp

    protected Date chatLockedUntil; // the user can't use the chat until timestamp

    protected Date lastConnection; // time of the last user connect

    public AuthorizedUser() {
    }

    public AuthorizedUser(String name, Hash hash, String email) {
        this.name = name;
        this.password = hash.toBase64();
        this.salt = hash.getSalt().toBase64();
        this.hashAlgorithm = hash.getAlgorithmName();
        this.hashIterations = hash.getIterations();
        this.email = email;
        this.chatLockedUntil = null;
        this.active = true;
        this.lockedUntil = null;
    }

    public boolean doCredentialsMatch(String name, String password) {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(this.hashAlgorithm);
        matcher.setHashIterations(this.hashIterations);
        AuthenticationToken token = new UsernamePasswordToken(name, password);
        AuthenticationInfo info = new SimpleAuthenticationInfo(this.name,
                ByteSource.Util.bytes(Base64.decode(this.password)),
                ByteSource.Util.bytes(Base64.decode(this.salt)), "");
        return matcher.doCredentialsMatch(token, info);
    }

    public String getName() {
        return this.name;
    }
}
