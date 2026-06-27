type OverlayCardProps = {
  title: string;
  subtitle: string;
  buttonText: string;
  handleClick: () => void;
};

const OverlayCard = ({
  title,
  subtitle,
  buttonText,
  handleClick,
}: OverlayCardProps) => {
  return (
    <div className="w-full md:w-1/2 md:h-full h-1/2 gap-4 bg-blue-800 flex flex-col justify-center items-center text-center">
      <h2 className="font-bold text-3xl">{title}</h2>
      <p className="font-light text-xs">{subtitle}</p>
      <button
        onClick={handleClick}
        className="cursor-pointer px-8 py-2 outline-white outline-1 rounded-lg font-medium"
      >
        {buttonText}
      </button>
    </div>
  );
};

export default OverlayCard;
