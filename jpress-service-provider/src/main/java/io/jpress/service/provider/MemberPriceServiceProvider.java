package io.jpress.service.provider;

import com.jfinal.plugin.activerecord.Db;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import io.jboot.utils.StrUtil;
import io.jpress.model.MemberPrice;
import io.jpress.service.MemberPriceService;

import java.math.BigDecimal;
import java.util.Date;

@Bean
public class MemberPriceServiceProvider extends JbootServiceBase<MemberPrice> implements MemberPriceService {

    @Override
    public MemberPrice findByPorductAndGroup(String productTableName, Object productId, Object groupId) {
        return findFirstByColumns(Columns.create("product_table", productTableName).eq("product_id", productId).eq("group_id", groupId));
    }


    @Override
    public void saveOrUpdateByProduct(String productTable, Long productId, String[] memberGroupIds, String[] memberGroupPrices) {
        if (memberGroupIds == null || memberGroupPrices == null || memberGroupIds.length == 0) {
            Db.update("delete from member_price where product_id = ?", productId);
            return;
        }

        //这种情况应该不可能出现
        if (memberGroupIds.length != memberGroupPrices.length) {
            return;
        }


        for (int i = 0; i < memberGroupIds.length; i++) {
            String memberGroupId = memberGroupIds[i];
            String memberGroupPrice = memberGroupPrices[i];

            MemberPrice existModel = findByPorductAndGroup(productTable, productId, memberGroupId);

            //删除之前的数据
            if (existModel != null && StrUtil.isBlank(memberGroupPrice)) {
                delete(existModel);
                continue;
            }

            //更新之前的数据
            else if (existModel != null && StrUtil.isNotBlank(memberGroupPrice)) {
                existModel.setPrice(new BigDecimal(memberGroupPrice));
                update(existModel);
                continue;
            }

            //创建新的数据
            else if (existModel == null && StrUtil.isNotBlank(memberGroupPrice)) {
                existModel = new MemberPrice();
                existModel.setProductTable(productTable);
                existModel.setProductId(productId);
                existModel.setGroupId(Long.valueOf(memberGroupId));
                existModel.setPrice(new BigDecimal(memberGroupPrice));
                existModel.setCreated(new Date());
                save(existModel);
            }

//            else if (existModel == null && StrUtil.isBlank(memberGroupPrice)) {}


        }
    }
}