// 메인 애플리케이션 로직
class BucketListApp {
    constructor() {
        this.currentFilter = 'all';
        this.editingId = null;
        this.init();
    }

    /**
     * 앱 초기화
     */
    init() {
        this.cacheElements();
        this.bindEvents();
        this.render();
    }

    /**
     * DOM 요소 캐싱
     */
    cacheElements() {
        // 폼 요소
        this.bucketForm = document.getElementById('bucketForm');
        this.bucketInput = document.getElementById('bucketInput');

        // 통계 요소
        this.totalCount = document.getElementById('totalCount');
        this.completedCount = document.getElementById('completedCount');
        this.progressCount = document.getElementById('progressCount');
        this.completionRate = document.getElementById('completionRate');

        // 리스트 컨테이너
        this.bucketListContainer = document.getElementById('bucketListContainer');
        this.emptyState = document.getElementById('emptyState');

        // 필터 버튼
        this.filterBtns = document.querySelectorAll('.filter-btn');

        // 모달 요소
        this.editModal = document.getElementById('editModal');
        this.editForm = document.getElementById('editForm');
        this.editInput = document.getElementById('editInput');
        this.cancelEditBtn = document.getElementById('cancelEdit');
    }

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        // 폼 제출 이벤트
        this.bucketForm.addEventListener('submit', (e) => this.handleAdd(e));

        // 필터 버튼 클릭 이벤트
        this.filterBtns.forEach(btn => {
            btn.addEventListener('click', (e) => this.handleFilter(e));
        });

        // 모달 이벤트
        this.editForm.addEventListener('submit', (e) => this.handleEditSubmit(e));
        this.cancelEditBtn.addEventListener('click', () => this.closeEditModal());
        this.editModal.addEventListener('click', (e) => {
            if (e.target === this.editModal) {
                this.closeEditModal();
            }
        });
    }

    /**
     * 새 버킷 리스트 추가 처리
     */
    handleAdd(e) {
        e.preventDefault();

        const title = this.bucketInput.value.trim();

        if (!title) {
            alert('버킷 리스트 내용을 입력해주세요!');
            return;
        }

        BucketStorage.addItem(title);
        this.bucketInput.value = '';
        this.bucketInput.focus();
        this.render();
    }

    /**
     * 필터 변경 처리
     */
    handleFilter(e) {
        const filter = e.target.dataset.filter;
        this.currentFilter = filter;

        // 필터 버튼 활성화 상태 변경
        this.filterBtns.forEach(btn => btn.classList.remove('active'));
        e.target.classList.add('active');

        this.render();
    }

    /**
     * 완료 상태 토글
     */
    handleToggle(id) {
        BucketStorage.toggleComplete(id);
        this.render();
    }

    /**
     * 수정 모달 열기
     */
    openEditModal(id, currentTitle) {
        this.editingId = id;
        this.editInput.value = currentTitle;
        this.editModal.classList.remove('hidden');
        this.editModal.classList.add('flex');
        this.editInput.focus();
    }

    /**
     * 수정 모달 닫기
     */
    closeEditModal() {
        this.editingId = null;
        this.editInput.value = '';
        this.editModal.classList.add('hidden');
        this.editModal.classList.remove('flex');
    }

    /**
     * 수정 제출 처리
     */
    handleEditSubmit(e) {
        e.preventDefault();

        const newTitle = this.editInput.value.trim();

        if (!newTitle) {
            alert('버킷 리스트 내용을 입력해주세요!');
            return;
        }

        if (this.editingId) {
            BucketStorage.updateItem(this.editingId, newTitle);
            this.closeEditModal();
            this.render();
        }
    }

    /**
     * 삭제 처리
     */
    handleDelete(id, title) {
        if (confirm(`"${title}"\n정말 삭제하시겠습니까?`)) {
            BucketStorage.deleteItem(id);
            this.render();
        }
    }

    /**
     * 통계 업데이트
     */
    updateStats() {
        const stats = BucketStorage.getStats();

        this.totalCount.textContent = stats.total;
        this.completedCount.textContent = stats.completed;
        this.progressCount.textContent = stats.progress;
        this.completionRate.textContent = `${stats.completionRate}%`;
    }

    /**
     * 버킷 리스트 항목 HTML 생성
     */
    createBucketItemHTML(item) {
        const completedClass = item.completed ? 'line-through text-gray-400' : 'text-gray-800';
        const checkIcon = item.completed ? '✓' : '';
        const checkboxClass = item.completed
            ? 'bg-green-500 border-green-500 text-white'
            : 'bg-white border-gray-300';

        return `
            <div class="bucket-item bg-white rounded-lg shadow-md p-4 flex items-center gap-3 hover:shadow-lg transition-shadow">
                <!-- 체크박스 -->
                <button
                    class="flex-shrink-0 w-6 h-6 rounded border-2 flex items-center justify-center ${checkboxClass} transition-all hover:scale-110"
                    onclick="app.handleToggle('${item.id}')"
                >
                    <span class="text-sm font-bold">${checkIcon}</span>
                </button>

                <!-- 제목 -->
                <div class="flex-1">
                    <p class="text-lg ${completedClass} break-words">${this.escapeHtml(item.title)}</p>
                    <p class="text-xs text-gray-400 mt-1">
                        ${new Date(item.createdAt).toLocaleDateString('ko-KR')} 생성
                        ${item.completedAt ? ` · ${new Date(item.completedAt).toLocaleDateString('ko-KR')} 완료` : ''}
                    </p>
                </div>

                <!-- 버튼 그룹 -->
                <div class="flex gap-2 flex-shrink-0">
                    <button
                        class="px-3 py-1 bg-blue-100 text-blue-600 rounded hover:bg-blue-200 transition-colors text-sm font-medium"
                        onclick="app.openEditModal('${item.id}', '${this.escapeHtml(item.title).replace(/'/g, "\\'")}')"
                    >
                        수정
                    </button>
                    <button
                        class="px-3 py-1 bg-red-100 text-red-600 rounded hover:bg-red-200 transition-colors text-sm font-medium"
                        onclick="app.handleDelete('${item.id}', '${this.escapeHtml(item.title).replace(/'/g, "\\'")}')"
                    >
                        삭제
                    </button>
                </div>
            </div>
        `;
    }

    /**
     * HTML 이스케이프 처리
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 화면 렌더링
     */
    render() {
        // 통계 업데이트
        this.updateStats();

        // 필터링된 리스트 가져오기
        const bucketList = BucketStorage.getFilteredList(this.currentFilter);

        // 리스트가 비어있으면 빈 상태 표시
        if (bucketList.length === 0) {
            this.bucketListContainer.innerHTML = '';
            this.emptyState.classList.remove('hidden');
            return;
        }

        // 빈 상태 숨기기
        this.emptyState.classList.add('hidden');

        // 리스트 렌더링
        const html = bucketList.map(item => this.createBucketItemHTML(item)).join('');
        this.bucketListContainer.innerHTML = html;
    }
}

// 앱 인스턴스 생성
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new BucketListApp();
});
