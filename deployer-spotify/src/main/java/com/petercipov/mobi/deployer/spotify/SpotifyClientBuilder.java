package com.petercipov.mobi.deployer.spotify;

import com.petercipov.mobi.config.ApiHost;
import com.petercipov.mobi.config.HttpRestApiHost;
import com.petercipov.mobi.config.HttpsRestApiHost;
import com.petercipov.mobi.config.UnixRestApiHost;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import java.nio.file.Paths;

/**
 *
 * @author Peter Cipov
 */
public class SpotifyClientBuilder implements ApiHost.Builder<DefaultDockerClient.Builder> {

	@Override
	public DefaultDockerClient.Builder build(HttpRestApiHost host) {
		return DefaultDockerClient
			.builder()
			.uri(host.getUri())
		;
	}

	@Override
	public DefaultDockerClient.Builder build(HttpsRestApiHost host) {
		DefaultDockerClient.Builder builder = DefaultDockerClient.builder();
			
		builder.uri(host.getUri());
		try {
			builder.dockerCertificates(new DockerCertificates(Paths.get(host.getCertPath())));			
		} catch(DockerCertificateException ex) {
			throw new IllegalArgumentException("Certificates could not be resolved", ex);
		}
		return builder;
	}

	@Override
	public DefaultDockerClient.Builder build(UnixRestApiHost host) {
		return DefaultDockerClient
			.builder()
			.uri(host.getUri())
		;
	}
}
