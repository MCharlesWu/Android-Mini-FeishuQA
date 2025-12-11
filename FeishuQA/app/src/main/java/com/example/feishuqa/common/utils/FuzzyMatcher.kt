package com.example.feishuqa.common.utils

/**
 * 模糊匹配工具类
 * 用于实现对话标题的模糊搜索功能
 */
object FuzzyMatcher {
    
    /**
     * 相似度阈值（0.0 - 1.0）
     * 当相似度 >= 此值时，认为匹配成功
     * 默认值 0.6 表示至少 60% 相似
     */
    private const val SIMILARITY_THRESHOLD = 0.6
    
    /**
     * 检查文本是否与关键词模糊匹配
     * 
     * @param text 要搜索的文本（如对话标题）
     * @param keyword 搜索关键词
     * @param threshold 相似度阈值（可选，默认使用 SIMILARITY_THRESHOLD）
     * @return 如果相似度 >= 阈值，返回 true；否则返回 false
     */
    fun isFuzzyMatch(
        text: String,
        keyword: String,
        threshold: Double = SIMILARITY_THRESHOLD
    ): Boolean {
        // 空字符串处理
        if (keyword.isBlank()) return true
        if (text.isBlank()) return false
        
        // 转换为小写进行不区分大小写的匹配
        val lowerText = text.lowercase()
        val lowerKeyword = keyword.lowercase()
        
        // 如果精确包含，直接返回 true（性能优化）
        if (lowerText.contains(lowerKeyword)) {
            return true
        }
        
        // 计算相似度
        val similarity = calculateSimilarity(lowerText, lowerKeyword)
        
        return similarity >= threshold
    }
    
    /**
     * 计算两个字符串的相似度（0.0 - 1.0）
     * 使用编辑距离算法（Levenshtein Distance）
     * 
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 相似度值，范围 [0.0, 1.0]，1.0 表示完全相同
     */
    fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        
        val maxLength = maxOf(s1.length, s2.length)
        val editDistance = levenshteinDistance(s1, s2)
        
        // 相似度 = 1 - (编辑距离 / 最大长度)
        return 1.0 - (editDistance.toDouble() / maxLength)
    }
    
    /**
     * 计算两个字符串的编辑距离（Levenshtein Distance）
     * 编辑距离：将一个字符串转换为另一个字符串所需的最少单字符编辑操作次数
     * 
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 编辑距离
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        
        // 创建动态规划表
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        // 初始化：空字符串到任意字符串的编辑距离
        for (i in 0..m) {
            dp[i][0] = i
        }
        for (j in 0..n) {
            dp[0][j] = j
        }
        
        // 填充动态规划表
        for (i in 1..m) {
            for (j in 1..n) {
                if (s1[i - 1] == s2[j - 1]) {
                    // 字符相同，不需要编辑
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    // 字符不同，取三种操作的最小值：
                    // 1. 删除 s1[i-1]：dp[i-1][j] + 1
                    // 2. 插入 s2[j-1]：dp[i][j-1] + 1
                    // 3. 替换 s1[i-1] 为 s2[j-1]：dp[i-1][j-1] + 1
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,      // 删除
                        dp[i][j - 1] + 1,      // 插入
                        dp[i - 1][j - 1] + 1   // 替换
                    )
                }
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * 计算相似度并返回结果（带相似度值）
     * 可用于排序：相似度高的排在前面
     * 
     * @param text 要搜索的文本
     * @param keyword 搜索关键词
     * @return 相似度值，范围 [0.0, 1.0]
     */
    fun getSimilarity(text: String, keyword: String): Double {
        if (keyword.isBlank()) return 1.0
        if (text.isBlank()) return 0.0
        
        val lowerText = text.lowercase()
        val lowerKeyword = keyword.lowercase()
        
        // 如果精确包含，返回较高的相似度
        if (lowerText.contains(lowerKeyword)) {
            // 计算包含位置的影响，完全匹配返回 1.0
            val index = lowerText.indexOf(lowerKeyword)
            if (index == 0 && lowerText.length == lowerKeyword.length) {
                return 1.0
            }
            // 开头匹配给予更高相似度
            return if (index == 0) 0.9 else 0.8
        }
        
        return calculateSimilarity(lowerText, lowerKeyword)
    }
}

