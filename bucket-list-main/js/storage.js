// LocalStorage 관리 모듈
const BucketStorage = {
    STORAGE_KEY: 'bucketList',

    /**
     * LocalStorage에서 버킷 리스트 불러오기
     * @returns {Array} 버킷 리스트 배열
     */
    load() {
        try {
            const data = localStorage.getItem(this.STORAGE_KEY);
            return data ? JSON.parse(data) : [];
        } catch (error) {
            console.error('데이터 로드 실패:', error);
            return [];
        }
    },

    /**
     * LocalStorage에 버킷 리스트 저장하기
     * @param {Array} bucketList - 저장할 버킷 리스트 배열
     * @returns {boolean} 저장 성공 여부
     */
    save(bucketList) {
        try {
            localStorage.setItem(this.STORAGE_KEY, JSON.stringify(bucketList));
            return true;
        } catch (error) {
            console.error('데이터 저장 실패:', error);
            return false;
        }
    },

    /**
     * 새 버킷 리스트 항목 추가
     * @param {string} title - 버킷 리스트 제목
     * @returns {Object} 추가된 항목
     */
    addItem(title) {
        const bucketList = this.load();
        const newItem = {
            id: Date.now().toString(),
            title: title.trim(),
            completed: false,
            createdAt: new Date().toISOString(),
            completedAt: null
        };
        bucketList.unshift(newItem); // 최신 항목을 맨 위에 추가
        this.save(bucketList);
        return newItem;
    },

    /**
     * 버킷 리스트 항목 수정
     * @param {string} id - 수정할 항목의 ID
     * @param {string} newTitle - 새로운 제목
     * @returns {boolean} 수정 성공 여부
     */
    updateItem(id, newTitle) {
        const bucketList = this.load();
        const index = bucketList.findIndex(item => item.id === id);

        if (index !== -1) {
            bucketList[index].title = newTitle.trim();
            this.save(bucketList);
            return true;
        }
        return false;
    },

    /**
     * 버킷 리스트 항목 삭제
     * @param {string} id - 삭제할 항목의 ID
     * @returns {boolean} 삭제 성공 여부
     */
    deleteItem(id) {
        const bucketList = this.load();
        const filteredList = bucketList.filter(item => item.id !== id);

        if (filteredList.length !== bucketList.length) {
            this.save(filteredList);
            return true;
        }
        return false;
    },

    /**
     * 완료 상태 토글
     * @param {string} id - 토글할 항목의 ID
     * @returns {boolean} 토글 후 완료 상태
     */
    toggleComplete(id) {
        const bucketList = this.load();
        const index = bucketList.findIndex(item => item.id === id);

        if (index !== -1) {
            bucketList[index].completed = !bucketList[index].completed;
            bucketList[index].completedAt = bucketList[index].completed
                ? new Date().toISOString()
                : null;
            this.save(bucketList);
            return bucketList[index].completed;
        }
        return false;
    },

    /**
     * 통계 정보 가져오기
     * @returns {Object} 통계 정보
     */
    getStats() {
        const bucketList = this.load();
        const total = bucketList.length;
        const completed = bucketList.filter(item => item.completed).length;
        const progress = total - completed;
        const completionRate = total > 0 ? Math.round((completed / total) * 100) : 0;

        return {
            total,
            completed,
            progress,
            completionRate
        };
    },

    /**
     * 필터링된 버킷 리스트 가져오기
     * @param {string} filter - 필터 타입 ('all', 'active', 'completed')
     * @returns {Array} 필터링된 버킷 리스트
     */
    getFilteredList(filter = 'all') {
        const bucketList = this.load();

        switch (filter) {
            case 'active':
                return bucketList.filter(item => !item.completed);
            case 'completed':
                return bucketList.filter(item => item.completed);
            default:
                return bucketList;
        }
    }
};
