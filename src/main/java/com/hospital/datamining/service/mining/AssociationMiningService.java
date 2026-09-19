package com.hospital.datamining.service.mining;

import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;

import java.util.List;
import java.util.Set;

public interface AssociationMiningService {
    /**
     * Khai phá tập phổ biến và luật kết hợp từ danh sách transactions dạng tên thuốc
     *
     * @param transactions Danh sách các giao dịch (mỗi transaction là tập tên thuốc)
     * @param parameters Các tham số minSupport, minConfidence, minLift, maxItemsetSize
     * @return MiningResult chứa danh sách frequent itemsets, association rules và số liệu đo đạc
     */
    MiningResult mine(List<Set<String>> transactions, MiningParameters parameters);

    /**
     * Khai phá tập phổ biến và luật kết hợp từ danh sách transactions dạng Medicine ID (mục 14 đề tài)
     *
     * @param transactions Danh sách các giao dịch (mỗi transaction là tập Medicine ID)
     * @param parameters Các tham số minSupport, minConfidence, minLift, maxItemsetSize
     * @return MiningResult chứa kết quả khai phá
     */
    MiningResult mineWithIds(List<Set<Long>> transactions, MiningParameters parameters);

    /**
     * Tên định danh của thuật toán
     */
    String getAlgorithmName();
}
