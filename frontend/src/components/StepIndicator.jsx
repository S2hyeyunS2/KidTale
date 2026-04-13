const STEPS = ['동화 정보 입력', '동화 미리보기', '배송 정보 입력', '주문 완료']

export default function StepIndicator({ current }) {
  return (
    <div className="flex items-center justify-center gap-0 py-6">
      {STEPS.map((label, idx) => {
        const step = idx + 1
        const isDone = step < current
        const isActive = step === current
        return (
          <div key={step} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-colors
                  ${isDone ? 'bg-primary text-white' : isActive ? 'bg-primary text-white ring-4 ring-primary/20' : 'bg-gray-100 text-gray-400'}`}
              >
                {isDone ? '✓' : step}
              </div>
              <span className={`mt-1 text-xs font-medium whitespace-nowrap
                ${isActive ? 'text-primary' : isDone ? 'text-primary/70' : 'text-gray-400'}`}>
                {label}
              </span>
            </div>
            {idx < STEPS.length - 1 && (
              <div className={`w-16 h-0.5 mb-5 mx-1 ${isDone ? 'bg-primary' : 'bg-gray-200'}`} />
            )}
          </div>
        )
      })}
    </div>
  )
}
