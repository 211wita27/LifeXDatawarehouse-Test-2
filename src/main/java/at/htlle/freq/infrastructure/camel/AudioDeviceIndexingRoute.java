package at.htlle.freq.infrastructure.camel;

import at.htlle.freq.domain.AudioDevice;
import at.htlle.freq.domain.AudioDeviceRepository;
import at.htlle.freq.infrastructure.lucene.LuceneIndexService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component("AudioDeviceIndexingRoute")
public class AudioDeviceIndexingRoute extends RouteBuilder {

    private final AudioDeviceRepository repo;
    private final LuceneIndexService lucene;

    public AudioDeviceIndexingRoute(AudioDeviceRepository repo, LuceneIndexService lucene) {
        this.repo = repo;
        this.lucene = lucene;
    }

    @Override
    public void configure() {
        // Periodisches Reindexing aller AudioDevices
        from("timer://idxAudioDevices?period=60000")
                .routeId("LuceneAudioDevicesReindex")
                .bean(repo, "findAll")
                .split(body())
                .process(ex -> {
                    AudioDevice d = ex.getIn().getBody(AudioDevice.class);
                    lucene.indexAudioDevice(
                            d.getAudioDeviceID() != null ? d.getAudioDeviceID().toString() : null,
                            d.getClientID() != null ? d.getClientID().toString() : null,
                            d.getAudioDeviceBrand(),
                            d.getDeviceType()
                    );
                })
                .end();

        // Indexing eines einzelnen AudioDevice
        from("direct:index-single-audiodevice")
                .routeId("LuceneIndexSingleAudioDevice")
                .process(ex -> {
                    AudioDevice d = ex.getIn().getBody(AudioDevice.class);
                    lucene.indexAudioDevice(
                            d.getAudioDeviceID() != null ? d.getAudioDeviceID().toString() : null,
                            d.getClientID() != null ? d.getClientID().toString() : null,
                            d.getAudioDeviceBrand(),
                            d.getDeviceType()
                    );
                });
    }
}
