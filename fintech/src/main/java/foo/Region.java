package foo;

import lombok.Getter;
import java.util.UUID;

@Getter
public enum Region {
    TULA("Tula", UUID.randomUUID()),

    TAMBOV("Tambov", UUID.randomUUID()),

    VLADIMIR("Vladimir", UUID.randomUUID()),

    BELGOROD("Belgorod", UUID.randomUUID()),

    MOSCOW("Moscow", UUID.randomUUID());

    Region(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    private final String name;
    private final UUID id;

}
