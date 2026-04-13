/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#E8526A',
        'primary-light': '#FF7A8A',
        'primary-dark': '#C43D54',
        cream: '#FFF8F9',
        'gray-soft': '#F5F5F5',
      },
      fontFamily: {
        sans: ['"Noto Sans KR"', 'sans-serif'],
      },
      boxShadow: {
        card: '0px 2px 12px 0px rgba(0, 0, 0, 0.10)',
        'card-hover': '0px 6px 24px 0px rgba(0, 0, 0, 0.15)',
      },
      borderRadius: {
        card: '15px',
      },
    },
  },
  plugins: [],
}
