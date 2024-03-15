package ru.mimicsmev.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.mimicsmev.dao.entity.VsList;
import ru.mimicsmev.dao.repository.VsListRepository;
import ru.mimicsmev.exception.AppException;

import java.util.Objects;

import static ru.mimicsmev.utils.Utils.trimValue;

@Component
public class VsListDao {

    private final VsListRepository vsListRepository;

    public VsListDao(VsListRepository vsListRepository) {
        this.vsListRepository = vsListRepository;
    }

    public Page<VsList> getList(int pageSize, int pageNum) {
        return vsListRepository.findAll(PageRequest.of(pageNum, pageSize, Sort.by(Sort.Order.desc("id"))));
    }

    public VsList addVs(String rootTag, String mnemonic, String mnemonicDesc, String desc) throws AppException {
        VsList c = vsListRepository.findVsListByRootTag(trimValue(rootTag));
        if (Objects.isNull(c)) {
            VsList vsList = new VsList();
            vsList.setRootTag(trimValue(rootTag));
            vsList.setMnemonic(trimValue(mnemonic));
            vsList.setMnemonicDesc(trimValue(mnemonicDesc));
            vsList.setDescription(desc);
            return vsListRepository.save(vsList);
        } else {
            throw new AppException(String.format("Root Tag already use: %s", c));
        }
    }
}
