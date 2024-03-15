package ru.mimicsmev.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import ru.mimicsmev.dao.entity.VsList;

import java.util.Objects;

@Configuration
@Getter
public class MimicProperties {
    @Value("${mimic.config.internal.mnemonic}")
    private String internalMnemonic;
    @Value("${mimic.config.internal.mnemonic-desc}")
    private String internalMnemonicDesc;
    private VsList internalVs;

    public VsList getInternalVs() {
        if(!Objects.isNull(internalVs) && StringUtils.hasLength(internalVs.getMnemonic())) {
            return internalVs;
        } else {
            internalVs = new VsList();
            internalVs.setMnemonic(this.internalMnemonic);
            internalVs.setMnemonicDesc(this.internalMnemonicDesc);
            return internalVs;
        }
    }
}
