import './css/Header.css';

// this the header on top
const Header = ({ onToggleSidebar, title = "Dashboard" }) => {
  return (
    <header className="admin-header">
      <button className="hamburger-btn" onClick={onToggleSidebar} aria-label="Toggle menu">
        <span className="hamburger-icon">☰</span>
      </button>
      <h1 className="header-title">{title}</h1>
    </header>
  );
};

export default Header;
