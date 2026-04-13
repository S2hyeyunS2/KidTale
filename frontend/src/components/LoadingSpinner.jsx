export default function LoadingSpinner({ message = '잠시만 기다려 주세요...' }) {
  return (
    <div className="fixed inset-0 z-50 bg-white/80 backdrop-blur-sm flex flex-col items-center justify-center gap-6">
      <div className="relative w-20 h-20">
        <div className="absolute inset-0 rounded-full border-4 border-primary/20" />
        <div className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin" />
        <div className="absolute inset-0 flex items-center justify-center text-2xl">✨</div>
      </div>
      <div className="text-center">
        <p className="text-gray-700 font-semibold text-lg">{message}</p>
        <div className="dot-bounce flex justify-center gap-2 mt-3">
          <span /><span /><span />
        </div>
      </div>
    </div>
  )
}
